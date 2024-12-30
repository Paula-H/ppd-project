package org.example;

import org.example.request.Request;
import org.example.request.RequestType;
import org.example.response.Response;
import org.example.response.ResponseType;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.Socket;

public class Main {
    private static final List<String> files = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        if (args.length < 1) {
            System.out.println("You have to provide the country code");
            System.exit(1);
        }
        if (Integer.parseInt(args[0]) >= Constants.COUNTRIES) {
            System.out.println("Invalid country code.");
            System.exit(1);
        }
        var countryCode = Integer.parseInt(args[0]);

        for (int i = 0; i < Constants.NO_OF_PROBLEMS; ++i) {
            files.add("C" + countryCode + "_P" + i + ".txt");
        }

        List<String> buffer = new ArrayList<>();
        for (String file : files) {
            File obj = new File(Constants.FILE_PATH + file);
            Scanner scanner = new Scanner(obj);

            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();

                buffer.add(data);

                if (buffer.size() == Constants.BATCH_SIZE) {
                    Request request = new Request(RequestType.SEND_PARTICIPANTS, buffer);

                    var response = sendRequest(request);
                    System.out.println("Received from server: " + response.getResponseType().toString());
                    buffer.clear();
                    Thread.sleep(Constants.DX * 1000);
                }
            }
        }
        if (!buffer.isEmpty()) {
            Request request = new Request(RequestType.SEND_PARTICIPANTS, buffer);

            var response = sendRequest(request);
            System.out.println("Received from server: " + response.getResponseType().toString());
            buffer.clear();
        }

        Request request = new Request(RequestType.GET_PARTIAL_COUNTRY_RANKING, null);

        Response partialRankingResponse = sendRequest(request);
        var partialCountryResults = partialRankingResponse.getPartialCountryResults();
        System.out.println("Partial Ranking:");
        for (var countryRank : partialCountryResults) {
            System.out.println(countryRank.getCountry() + " " + countryRank.getScore());
        }

        Request finalResultsRequest = new Request(RequestType.GET_FINAL_COUNTRY_RANKING, null);
        Response finalRankingResponse = sendRequest(finalResultsRequest);

        while (finalRankingResponse.getResponseType() == ResponseType.ERROR) {
            System.out.println("Received error from server. Trying again ... \n");
            Thread.sleep(Constants.DT * 1000);
            finalRankingResponse = sendRequest(finalResultsRequest);
        }

        System.out.println("Received final rankings from server. Saving them ... \n");

        var finalParticipantsRanking = finalRankingResponse.getFinalParticipantsRankingFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(finalParticipantsRanking.getFileName())) {
            fileOutputStream.write(finalParticipantsRanking.getFileData());
            System.out.println("File saved successfully: " + finalParticipantsRanking.getFileName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var finalCountriesRanking = finalRankingResponse.getFinalCountriesRankingFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(finalCountriesRanking.getFileName())) {
            fileOutputStream.write(finalCountriesRanking.getFileData());
            System.out.println("File saved successfully: " + finalCountriesRanking.getFileName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Closing client ... \n");
    }

    public static Response sendRequest(Request request) {
        try (Socket socket = new Socket("127.0.0.1", 12345); ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(request);
            out.flush();
            Response response = (Response) in.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}