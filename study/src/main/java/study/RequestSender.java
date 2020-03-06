package study;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility class that send http requests.
 */
public class RequestSender {

    /**
     * Send a http GET request.
     *
     * @param url The url of the request.
     * @return The raw response message.
     */
    public static String sendGetRequest(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Authorization", "Token " + System.getenv("GITHUB_TOKEN"));
        con.setRequestMethod("GET");
        if (con.getResponseCode() != 200) {
            throw new RuntimeException("Received " + con.getResponseCode() + " from " + url);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine = null;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        return response.toString();
    }

}
