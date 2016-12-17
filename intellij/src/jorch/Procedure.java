package jorch;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class Procedure {
    private String name;

    public Procedure(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUrl() throws UnsupportedEncodingException, URISyntaxException {
        return new URI("http", null, "localhost", 8000, "/test/ProcedureList/startProcedure", "0=" + URLEncoder.encode(getName(), "ISO-8859-1"), null).toString();
        //return "http://localhost:8000/test/Test1/ProcedureList/" + URLEncoder.encode(getName(), "ISO-8859-1");
    }
}
