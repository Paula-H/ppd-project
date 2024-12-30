package org.example;

import org.example.linked_list.LinkedList;
import org.example.linked_list.LinkedListElement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class WorkerThread extends Thread{
    private BlockingQueue<LinkedListElement> queue;
    private LinkedList list;
    private AtomicInteger countriesThatGotPartialRanking;
    private final Logger LOGGER = Logger.getLogger(WorkerThread.class.getName());

    public WorkerThread(BlockingQueue<LinkedListElement> queue, LinkedList list, AtomicInteger countriesThatGotPartialRanking) {
        this.queue = queue;
        this.list = list;
        this.countriesThatGotPartialRanking = countriesThatGotPartialRanking;
    }

    @Override
    public void run() {
        LOGGER.entering(WorkerThread.class.getName(), "run");
        while (true) {
            LinkedListElement element = null;
            try {
                element = queue.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (element == null && countriesThatGotPartialRanking.get() < Constants.COUNTRIES * (Constants.NO_OF_PROBLEMS + 1)) {
                continue;
            }
            if (element == null) {
                break;
            }
            list.add(element);
        }
        LOGGER.exiting(WorkerThread.class.getName(), "run");
    }
}
