package edu.escuelaing.arem.ASE.app;

import java.io.IOException;
import java.net.URISyntaxException;

public class MyWebServices {
    public static void main(String[] args) throws IOException, URISyntaxException {
        HttpServer.get("/arep", () -> {
            String resp = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type:text/html; charset=utf-8\r\n"
                    + "\r\n"
                    + "Hello AREP.";
            return resp;
        });

        HttpServer.get("/ieti", () -> {
            String resp = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type:text/html; charset=utf-8\r\n"
                    + "\r\n"
                    + "Hello IETI.";
            return resp;
        });

        HttpServer.getInstance().runServer(args);
    }
}
