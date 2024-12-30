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
import java.util.logging.Logger;

public class Main {
    private static final LinkedList resultList = new LinkedList();
    private static final ExecutorService executor = Executors.newFixedThreadPool(Constants.READERS);
    private static BlockingQueue<LinkedListElement> workingQueue = new LinkedBlockingQueue<>();
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LOGGER.entering(Main.class.getName(), "main");

        Thread[] writersThreads = new Thread[Constants.WRITERS];

        AtomicInteger countriesThatGotPartialRanking = new AtomicInteger(0);
        AtomicInteger countriesThatGotFinalRanking = new AtomicInteger(0);

        LOGGER.info("Initializing and starting writers ... \n");

        for (int i = 0; i < Constants.WRITERS; ++i) {
            Thread thread = new WorkerThread(workingQueue, resultList, countriesThatGotPartialRanking);
            writersThreads[i] = thread;
        }

        Arrays.stream(writersThreads).forEach(Thread::start);

        try (ServerSocket serverSocket = new ServerSocket(Constants.PORT)) {
            LOGGER.info("Server started, listening on port " + Constants.PORT);
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
                    LOGGER.info("Exception caught when trying to listen on port " + Constants.PORT + ". Exiting ... \n");
                    LOGGER.throwing(Main.class.getName(), "main", e);
                } catch (InterruptedException e) {
                    LOGGER.throwing(Main.class.getName(), "main", e);
                }
            }
        } catch (IOException e) {
            LOGGER.info("Could not listen on port " + Constants.PORT + ". Exiting ... \n");
            LOGGER.throwing(Main.class.getName(), "main", e);
        }
        finally {
            LOGGER.info("Closing readers ... \n");
            executor.shutdown();
        }

        Arrays.stream(writersThreads).forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        LOGGER.info("Server stopped.");
    }
}