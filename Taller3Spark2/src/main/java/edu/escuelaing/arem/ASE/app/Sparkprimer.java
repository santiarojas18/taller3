package edu.escuelaing.arem.ASE.app;

import static spark.Spark.*;

public class Sparkprimer {
    public static void main(String[] args) {
        get("/hello", (req, res) -> {

            String p = req.queryParams("val");
            String msg = "<h1>Hello World</h1>" + p;
            return msg;
        });
    }
}
