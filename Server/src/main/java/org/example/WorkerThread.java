package org.example;

import org.example.linked_list.LinkedList;
import org.example.linked_list.LinkedListElement;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerThread extends Thread{
    private BlockingQueue<LinkedListElement> queue;
    private LinkedList list;
    private AtomicInteger countriesThatGotPartialRanking;
    public WorkerThread(BlockingQueue<LinkedListElement> queue, LinkedList list, AtomicInteger countriesThatGotPartialRanking) {
        this.queue = queue;
        this.list = list;
        this.countriesThatGotPartialRanking = countriesThatGotPartialRanking;
    }

    @Override
    public void run() {
        while (true) {
            LinkedListElement element = null;
            try {
                element = queue.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (element == null && countriesThatGotPartialRanking.get() < Constants.COUNTRIES) {
                continue;
            }
            if (element == null) {
                break;
            }
            list.add(element);
        }
    }
}
