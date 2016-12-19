package jorch;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.function.Supplier;

public class Procedure {
    private String name;
    private Supplier<TaskSelector> taskSelectorSupplier;

    public Procedure(String name, Supplier<TaskSelector> taskSelectorSupplier) {
        this.name = name;
        this.taskSelectorSupplier = taskSelectorSupplier;
    }

    public String getName() {
        return name;
    }

    public String getUrl() throws UnsupportedEncodingException, URISyntaxException {
        return new URI("http", null, "localhost", 8000, "/test/ProcedureList/startProcedure", "0=" + URLEncoder.encode(getName(), "ISO-8859-1"), null).toString();
    }

    public TaskSelector getTaskSelector() {
        return taskSelectorSupplier.get();
    }
}
