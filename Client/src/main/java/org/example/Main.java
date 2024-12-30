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
import java.util.logging.Logger;

public class Main {
    private static final List<String> files = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        LOGGER.entering(Main.class.getName(), "main");

        if (args.length < 1) {
            LOGGER.throwing(Main.class.getName(), "main", new Exception("You have to provide the country code"));
            System.exit(1);
        }
        if (Integer.parseInt(args[0]) >= Constants.COUNTRIES) {
            LOGGER.throwing(Main.class.getName(), "main", new Exception("Invalid country code."));
            System.exit(1);
        }
        var countryCode = Integer.parseInt(args[0]);

        for (int i = 0; i < Constants.NO_OF_PROBLEMS; ++i) {
            files.add("C" + countryCode + "_P" + i + ".txt");
        }

        LOGGER.info("Reading files and sending data to server ... \n");

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

                    if (response.getResponseType() == ResponseType.ERROR) {
                        LOGGER.throwing(Main.class.getName(), "main", new Exception("Received error from server. Trying again ..."));
                        Thread.sleep(Constants.DT * 1000);
                        continue;
                    }

                    buffer.clear();
                    Thread.sleep(Constants.DX * 1000);
                }
            }
        }
        if (!buffer.isEmpty()) {
            Request request = new Request(RequestType.SEND_PARTICIPANTS, buffer);
            var response = sendRequest(request);

            if (response.getResponseType() == ResponseType.ERROR) {
                LOGGER.throwing(Main.class.getName(), "main", new Exception("Received error from server. Trying again ..."));
                Thread.sleep(Constants.DT * 1000);
            }

            buffer.clear();
        }

        Request request = new Request(RequestType.GET_PARTIAL_COUNTRY_RANKING, null);
        LOGGER.info("Sending to server the request for the partial countries ranking ... \n");
        Response partialRankingResponse = sendRequest(request);
        var partialCountryResults = partialRankingResponse.getPartialCountryResults();

        LOGGER.info("Received partial country ranking from server. Printing it ... \n");
        for (var countryRank : partialCountryResults) {
            LOGGER.info(countryRank.getCountry() + " " + countryRank.getScore());
        }

        Request finalResultsRequest = new Request(RequestType.GET_FINAL_COUNTRY_RANKING, null);
        Response finalRankingResponse = sendRequest(finalResultsRequest);

        while (finalRankingResponse.getResponseType() == ResponseType.ERROR) {
            LOGGER.throwing(Main.class.getName(), "main", new Exception("Received error from server. Trying again ..."));
            Thread.sleep(Constants.DT * 1000);
            finalRankingResponse = sendRequest(finalResultsRequest);
        }

        LOGGER.info("Received final rankings from server. Saving them ... \n");

        var finalParticipantsRanking = finalRankingResponse.getFinalParticipantsRankingFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(finalParticipantsRanking.getFileName())) {
            fileOutputStream.write(finalParticipantsRanking.getFileData());
            LOGGER.info("File saved successfully: " + finalParticipantsRanking.getFileName());
        } catch (IOException e) {
            LOGGER.throwing(Main.class.getName(), "main", new Exception(e));
        }

        var finalCountriesRanking = finalRankingResponse.getFinalCountriesRankingFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(finalCountriesRanking.getFileName())) {
            fileOutputStream.write(finalCountriesRanking.getFileData());
            LOGGER.info("File saved successfully: " + finalCountriesRanking.getFileName());
        } catch (IOException e) {
            LOGGER.throwing(Main.class.getName(), "main", new Exception(e));
        }

        LOGGER.exiting(Main.class.getName(), "main");
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