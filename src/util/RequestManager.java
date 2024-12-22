package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class RequestManager {

    HttpClient client;
    String statusURL;
    String gameURL;

    static String TOKEN = "TOKEN";
    static String STATUS_TEST_URL = "https://games-test.datsteam.dev/rounds/snake3d/";
    static String STATUS_PROD_URL = "https://games.datsteam.dev/rounds/snake3d/";
    static String GAME_TEST_URL = "https://games-test.datsteam.dev/play/snake3d/player/move";
    static String GAME_PROD_URL = "https://games.datsteam.dev/play/snake3d/player/move";

    public RequestManager(boolean runProd) {
        client = HttpClient.newBuilder().build();
        statusURL = runProd ? STATUS_PROD_URL : STATUS_TEST_URL;
        gameURL = runProd ? GAME_PROD_URL : GAME_TEST_URL;
    }

    public String sendRequest(String body) throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader("Content-Type", "application/json")
                .uri(new URI(gameURL))
                .headers(
                        "Content-Type", "application/json",
                        "X-Auth-Token", TOKEN,
                        "Accept-Encoding", "gzip, deflate"
                )
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse.BodyHandler<InputStream> handler = HttpResponse.BodyHandlers.ofInputStream();
        HttpResponse<InputStream> response = client.send(request, handler);

        InputStream inputStream = response.body();
        String encoding = response.headers().firstValue("Content-Encoding").orElse(null);
        if ("gzip".equalsIgnoreCase(encoding)) {
            inputStream = new GZIPInputStream(inputStream);
        } else if ("deflate".equalsIgnoreCase(encoding)) {
            inputStream = new InflaterInputStream(inputStream);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder responseBodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBodyBuilder.append(line);
        }
        reader.close();
        String responseBody = responseBodyBuilder.toString();

        return responseBody;
    }

    public void printStatus() throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .setHeader("Content-Type", "application/json")
                .uri(new URI(statusURL))
                .headers("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, handler);

        System.out.println(response.body());
    }
}
