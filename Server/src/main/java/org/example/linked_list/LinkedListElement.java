package org.example.linked_list;

import java.util.concurrent.locks.ReentrantLock;

public class LinkedListElement {
    public String participant;
    public Integer score;
    public String country;
    public LinkedListElement next;
    final ReentrantLock lock = new ReentrantLock();

    public LinkedListElement(String participant, Integer score, String country) {
        this.participant = participant;
        this.score = score;
        this.country = country;
        this.next = null;
    }
}
