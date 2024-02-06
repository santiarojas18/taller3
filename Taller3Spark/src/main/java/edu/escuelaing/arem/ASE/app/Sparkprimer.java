package edu.escuelaing.arem.ASE.app;

import static spark.Spark.*;

public class Sparkprimer {
    public static void main(String[] args) {
        get("/hello", (req, res) -> {
            String msg = "<h1>Hello World</h1>";
            return msg;
        });
    }
}
