package com.trumedia.project;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;

import org.json.JSONObject;

public class App {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        try {
            testStatsAPI();
        } catch (Exception e) {
            System.err.println("Something went wrong");
            e.printStackTrace();
        }
    }

    private static void testStatsAPI() throws URISyntaxException, IOException, InterruptedException {
        String isoDate = "2024-06-18";
        var client = HttpClient.newBuilder().build();
        var uri = new URI("https://statsapi.mlb.com/api/v1/schedule?sportId=1&date="+isoDate);
        var request = HttpRequest.newBuilder(uri).build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString(Charset.defaultCharset()));
        JSONObject scheduleData = new JSONObject(response.body());
        int gameCount = scheduleData.getInt("totalGames");
        System.out.println("Found "+gameCount+" total games on this date.");

        File outFile = new File("test.csv");
        CSVWriter writer = new CSVWriter(outFile);
        String[] columns = {"date", "games"};
        CSVData csv = new CSVData(columns);
        String[] data = {isoDate, String.valueOf(gameCount)};
        csv.addLine(data);
        System.out.println("Writing CSV file...");
        writer.write(csv, true, true);
        System.out.println("Done");
    }
}
