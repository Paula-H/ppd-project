package org.example;

import org.example.linked_list.LinkedList;
import org.example.linked_list.LinkedListElement;
import org.example.request.Request;
import org.example.request.RequestType;
import org.example.response.CountryRank;
import org.example.response.FileWrapper;
import org.example.response.Response;
import org.example.response.ResponseType;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientRunnable implements Runnable{
    private final ExecutorService executor;
    private final Socket clientSocket;
    private BlockingQueue<LinkedListElement> workingQueue;
    private LinkedList resultList;
    private List<CountryRank> partialCountryResults;
    private AtomicInteger countriesThatGotPartialRanking;
    private AtomicInteger countriesThatGotFinalRanking;

    public ClientRunnable(
            ExecutorService executor,
            Socket clientSocket,
            BlockingQueue<LinkedListElement> workingQueue,
            LinkedList resultList,
            AtomicInteger countriesThatGotPartialRanking,
            AtomicInteger countriesThatGotFinalRanking
    ) {
        this.executor = executor;
        this.clientSocket = clientSocket;
        this.workingQueue = workingQueue;
        this.resultList = resultList;
        this.countriesThatGotPartialRanking = countriesThatGotPartialRanking;
        this.countriesThatGotFinalRanking = countriesThatGotFinalRanking;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream()))
        {
            Request request = (Request) in.readObject();
            System.out.println("Received request: " + request.getRequestType().toString());
            try {
                if (request.getRequestType() == RequestType.SEND_PARTICIPANTS) {
                    var data = request.getData();
                    data.forEach(result -> {
                        var parts = result.split(" ");
                        var participant = parts[0];
                        var country = participant.substring(1,2);
                        var score = parts[1];
                        var element = new LinkedListElement(participant, Integer.parseInt(score), country);
                        try {
                            workingQueue.put(element);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    out.writeObject(new Response(
                            ResponseType.OK,
                            null,
                            null,
                            null
                    ));
                    out.flush();
                } else if (request.getRequestType() == RequestType.GET_PARTIAL_COUNTRY_RANKING) {
                    long start = 0, end = Constants.DT;

                    while (end - start >= Constants.DT || partialCountryResults == null) {
                        start = System.currentTimeMillis();
                        Future<List<CountryRank>> partialFutureResult =
                                executor.submit(() -> resultList.getCountryRanks());

                        partialCountryResults = partialFutureResult.get();
                        end = System.currentTimeMillis();

                        if (end - start < Constants.DT) {
                            out.writeObject(new Response(
                                    ResponseType.OK,
                                    partialCountryResults,
                                    null,
                                    null));
                            out.flush();
                            countriesThatGotPartialRanking.incrementAndGet();
                            if (countriesThatGotPartialRanking.get() == Constants.COUNTRIES) {
                                var participantsSorted = resultList.getSortedList();
                                try (BufferedWriter bw = new BufferedWriter(new FileWriter("participantsRankingResults.txt"))) {
                                    for (var element : participantsSorted) {
                                        bw.write(element.participant + " " + element.score + " " + element.country + "\n");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                var countriesSorted = resultList.getCountryRanks();
                                try (BufferedWriter bw = new BufferedWriter(new FileWriter("countriesRankingResults.txt"))) {
                                    for (var countryRank : countriesSorted) {
                                        bw.write(countryRank.getCountry() + " " + countryRank.getScore() + "\n");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                    }
                } else if (request.getRequestType() == RequestType.GET_FINAL_COUNTRY_RANKING) {
                    if (countriesThatGotPartialRanking.get() != Constants.COUNTRIES) {
                        out.writeObject(new Response(ResponseType.ERROR, null, null, null));
                        out.flush();
                        return;
                    }

                    FileWrapper finalParticipantsRanking = null;
                    try {
                        byte[] fileData = Files.readAllBytes(Path.of("participantsRankingResults.txt"));
                        finalParticipantsRanking = new FileWrapper("participantsRankingResults.txt", fileData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    FileWrapper finalCountriesRanking = null;
                    try {
                        byte[] fileData = Files.readAllBytes(Path.of("countriesRankingResults.txt"));
                        finalCountriesRanking = new FileWrapper("countriesRankingResults.txt", fileData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    out.writeObject(new Response(ResponseType.OK, resultList.getCountryRanks(), finalParticipantsRanking, finalCountriesRanking));
                    out.flush();
                    countriesThatGotFinalRanking.incrementAndGet();
                }
            } catch (Exception e) {
                e.printStackTrace();
                out.writeObject(new Response(
                        ResponseType.ERROR,
                        null,
                        null,
                        null
                ));
                out.flush();
            }
            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}