package org.example;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class FileWorker {
    public static void assertEquality(String fileName1, String fileName2) throws FileNotFoundException {
        java.io.File file1 = new java.io.File(fileName1);
        java.io.File file2 = new java.io.File(fileName2);
        java.util.Scanner input1 = new java.util.Scanner(file1);
        java.util.Scanner input2 = new java.util.Scanner(file2);
        Map<String, String> map1 = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();
        while (input1.hasNext() && input2.hasNext()) {
            String participant1 = input1.next();
            String score1 = input1.next();
            String country1 = input1.next();
            String participant2 = input2.next();
            String score2 = input2.next();
            map1.put(participant1, score1);
            map2.put(participant2, score2);
        }

        if (input1.hasNext() || input2.hasNext()) {
            throw new RuntimeException("Files have different number of lines");
        }

        for (String key : map1.keySet()) {
            if (!map1.get(key).equals(map2.get(key))) {
                throw new RuntimeException("Files have different content");
            }
        }
    }
}
