package org.example.linked_list;

import org.example.response.CountryRank;

import java.util.ArrayList;

public interface LinkedListInterface {
    LinkedListElement getHead();
    void add(LinkedListElement element);
    void remove(String key);
    LinkedListElement search(String key);
    ArrayList<LinkedListElement> getSortedList();
    ArrayList<CountryRank> getCountryRanks();
}
