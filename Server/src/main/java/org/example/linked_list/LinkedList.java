package org.example.linked_list;

import org.example.response.CountryRank;
import java.util.*;
import java.util.logging.Logger;

public class LinkedList implements LinkedListInterface {
    private LinkedListElement head;
    private LinkedListElement tail;
    private Set<String> blackList;
    private final Logger LOGGER = Logger.getLogger(LinkedList.class.getName());

    public LinkedList() {
        LOGGER.entering(LinkedList.class.getName(), "LinkedList");
        LOGGER.info("Creating new linked list ... \n");
        this.head = new LinkedListElement("", 0, "");
        this.tail = new LinkedListElement("", 0, "");
        this.head.next = this.tail;
        this.blackList = new HashSet<>();
    }

    @Override
    public LinkedListElement getHead() {
        return this.head.next;
    }

    @Override
    public void add(LinkedListElement element) {
        head.lock.lock();
        LinkedListElement prev = head;
        LinkedListElement curr = head.next;
        try {
            while (curr != tail) {
                curr.lock.lock();
                try {
                    if(blackList.contains(element.participant)){
                        return;
                    }
                    if(element.score < 0){
                        blackList.add(element.participant);
                        this.remove(element.participant);
                        return;
                    }
                    if (curr.participant.equals(element.participant)) {
                        curr.score += element.score;
                        return;
                    }
                } finally {
                    prev.lock.unlock();
                    prev = curr;
                }
                curr = curr.next;
            }
            element.next = tail;
            prev.next = element;
        } finally {
            prev.lock.unlock();
        }
    }

    @Override
    public void remove(String key) {
        this.head.lock.lock();
        LinkedListElement prev = this.head;
        LinkedListElement current = this.head.next;
        try {
            while (current != null) {
                current.lock.lock();
                try {
                    if (current.participant.equals(key)) {
                        prev.next = current.next;
                        return;
                    }
                } finally {
                    prev.lock.unlock();
                    prev = current;
                }
                current = current.next;
            }
        } finally {
            prev.lock.unlock();
        }
    }

    @Override
    public LinkedListElement search(String key) {
        head.lock.lock();
        try {
            LinkedListElement current = head.next;
            while (current != null) {
                current.lock.lock();
                try {
                    if (current.participant.equals(key)) {
                        return current;
                    }
                } finally {
                    current.lock.unlock();
                }
                current = current.next;
            }
        } finally {
            head.lock.unlock();
        }
        return null;
    }

    @Override
    public ArrayList<LinkedListElement> getSortedList() {
        LOGGER.entering(LinkedList.class.getName(), "getSortedList");
        LOGGER.info("Getting the sorted participants list ... \n");
        ArrayList<LinkedListElement> sortedList = new ArrayList<>();
        LinkedListElement current = head.next;
        while (current != tail) {
            sortedList.add(current);
            current = current.next;
        }
        sortedList.sort((a, b) -> b.score - a.score);
        LOGGER.exiting(LinkedList.class.getName(), "getSortedList");
        return sortedList;
    }

    @Override
    public ArrayList<CountryRank> getCountryRanks() {
        LOGGER.entering(LinkedList.class.getName(), "getCountryRanks");
        LOGGER.info("Getting country ranks ... \n");
        Map<String, Integer> countryScores = new HashMap<>();
        for (var participant : getSortedList()) {
            countryScores.merge(participant.country, participant.score, Integer::sum);
        }
        ArrayList<CountryRank> countryRanks = new ArrayList<>();
        for (var entry : countryScores.entrySet()) {
            countryRanks.add(new CountryRank(entry.getKey(), entry.getValue()));
        }
        countryRanks.sort((a, b) -> b.getScore() - a.getScore());
        LOGGER.exiting(LinkedList.class.getName(), "getCountryRanks");
        return countryRanks;
    }
}