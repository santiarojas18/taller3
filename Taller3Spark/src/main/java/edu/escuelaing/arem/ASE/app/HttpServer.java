package edu.escuelaing.arem.ASE.app;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hello world!
 *
 */
public class HttpServer
{
    private static HttpServer _instance = new HttpServer();
    private static ConcurrentHashMap<String, StringBuffer> cache;
    private static Map<String,WebService> services = new HashMap<String, WebService>();

    private HttpServer(){}

    public static HttpServer getInstance() {
        return _instance;
    }
    public void runServer(String[] args) throws IOException, URISyntaxException {
        cache = new ConcurrentHashMap<>();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine;
            String outputLine = "";

            boolean firstLine = true;
            String uriStr = "";

            while ((inputLine = in.readLine()) != null) {
                if(firstLine){
                    uriStr = inputLine.split(" ")[1];
                    firstLine = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            URI fileUri = new URI(uriStr);

            //EXTERNAL API CONNECTION
            HttpConnectionExample connectionToApi = new HttpConnectionExample();
            System.out.println(fileUri.getPath());

            String path = fileUri.getPath();
            if (uriStr.startsWith("/movies/")) {
                String[] pathAPI = fileUri.getPath().split("/");
                String movieTitle = pathAPI[2];
                outputLine = apiSearcher(connectionToApi, movieTitle);
            } else if (path.startsWith("/action")){
                String webUri = path.replace("/action", "");
                if (services.containsKey(webUri)) {
                    //Quitar query, se envia el param
                    outputLine = services.get(webUri).handle();
                }
            } else if (!uriStr.startsWith("/favicon")){
                outputLine = httpFileSearcher(fileUri.getPath(), clientSocket);
            } else {
                outputLine = "";
            }
            out.println(outputLine);

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String httpError() throws IOException {
        Path file = Paths.get("target/classes/public/error.html");
        String contentType = Files.probeContentType(file);

        String outputLine = "HTTP/1.1 404 NOT FOUND\r\n"
                + "Content-Type:" + contentType + ";charset=utf-8\r\n"
                + "\r\n";

        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                outputLine = outputLine + line;
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        return outputLine;

    }

    public static String httpFileSearcher(String fileName, Socket clientSocket) throws IOException {
        Path file = Paths.get("target/classes/public" + fileName);
        String contentType = Files.probeContentType(file);
        //System.out.println("EL CONTENT TYPE: " + contentType);
        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type:" + contentType + ";charset=utf-8\r\n"
                + "\r\n";

        if (contentType != null && contentType.contains("image")) {
            imagesReader(file, clientSocket.getOutputStream());
            outputLine = "";
        } else {
            Charset charset = Charset.forName("UTF-8");
            try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    outputLine = outputLine + line;
                }
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
                outputLine = httpError();
            }
        }
        return outputLine;
    }

    public static void imagesReader(Path file, OutputStream outputStream) throws IOException {
        String contentType = Files.probeContentType(file);
        try (InputStream inputStream = Files.newInputStream(file)) {
            String header = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type:" + contentType + "\r\n"
                    + "Content-Length: " + Files.size(file) + "\r\n"
                    + "\r\n";
            outputStream.write(header.getBytes());

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        outputStream.close();
    }

    private static String apiSearcher(HttpConnectionExample connectionToApi, String movieTitle) throws IOException {
        StringBuffer apiResponse = new StringBuffer();
        String outputLine;
        //Verifies if the movie title has not been requested
        if (!cache.containsKey(movieTitle)) {
            //External API request
            try {
                apiResponse = connectionToApi.getMovieInfo(movieTitle);
                cache.put(movieTitle, apiResponse);
            } catch (IOException e) {
                System.err.format("IOException: %s%n", e);
                outputLine = httpError();
            }
        } else {
            apiResponse = cache.get(movieTitle);
        }

        outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type:application/json; charset=utf-8\r\n"
                + "\r\n"
                + apiResponse.toString();
        return outputLine;
    }

    public static void get(String r, WebService s) {
        services.put(r, s);
    }
}
