package edu.escuelaing.arem.ASE.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class HttpConnectionExample {
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String GET_URL = "http://www.omdbapi.com/?apikey=468842bf&t=";

    public HttpConnectionExample() {
    }

    public StringBuffer getMovieInfo(String movieName) throws IOException {
        String finalUrl = GET_URL + movieName;
        URL obj = new URL(finalUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        //The following invocation perform the connection implicitly before getting the code
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

        //Answer
        StringBuffer response = new StringBuffer();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;


            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            //System.out.println(response.toString());
        } else {
            response.append("GET request not worked");
        }
        System.out.println("GET DONE");

        //Return result
        return response;
    }
}