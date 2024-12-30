package org.example;

import org.example.linked_list.LinkedList;
import org.example.linked_list.LinkedListElement;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int readers = 4;
    private static final int writers = 8;
    private static final LinkedList resultList = new LinkedList();
    private static final int PORT = 12345;
    private static final ExecutorService executor = Executors.newFixedThreadPool(readers);
    private static BlockingQueue<LinkedListElement> workingQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws InterruptedException {
        Thread[] writersThreads = new Thread[writers];

        AtomicInteger countriesThatGotPartialRanking = new AtomicInteger(0);
        AtomicInteger countriesThatGotFinalRanking = new AtomicInteger(0);

        for (int i = 0; i < writers; ++i) {
            Thread thread = new WorkerThread(workingQueue, resultList, countriesThatGotPartialRanking);
            writersThreads[i] = thread;
        }

        Arrays.stream(writersThreads).forEach(Thread::start);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started, listening on port " + PORT);
            while (countriesThatGotFinalRanking.get() != Constants.COUNTRIES) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    executor.submit(new ClientRunnable
                            (
                                    executor,
                                    clientSocket,
                                    workingQueue,
                                    resultList,
                                    countriesThatGotPartialRanking,
                                    countriesThatGotFinalRanking
                            ));
                    Thread.sleep(500);
                } catch (IOException e) {
                    System.err.println("Exception caught when trying to listen on port " + PORT + " or listening for a connection");
                    System.err.println(e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
            System.err.println(e.getMessage());
        }
        finally {
            System.out.println("Closing readers ... \n");
            executor.shutdown();
        }

        Arrays.stream(writersThreads).forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Server stopped.");
    }
}