package vers;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Repository {
    private String filePath;
    private Object root;
    private ScriptEngineManager engineManager;
    private NashornScriptEngine engine;

    public Repository(String filePath, Object root) {
        this.filePath = filePath;
        this.root = root;
    }

    private NashornScriptEngine getEngine() {
        if(engine == null) {
            engineManager = new ScriptEngineManager();
            engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
            engine.put("root", root);

            Arrays.asList(root.getClass().getDeclaredMethods()).stream().forEach(x -> {
                String params = IntStream.range(0, x.getParameterCount()).mapToObj(i -> "arg" + i).collect(Collectors.joining(", "));
                try {
                    String setFunction = x.getName() + " = function(" + params + ") { return root." + x.getName() + "(" + params + ") }";
                    engine.eval(setFunction);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            });
        }

        return engine;
    }

    private void load() {
        try {
            DataInputStream inputStream = new DataInputStream(newInputStream(Paths.get(filePath)));
            while(inputStream.available() > 0) {
                String text = inputStream.readUTF();
                try {
                    getEngine().eval(text);
                } catch (ScriptException e2) {
                    e2.printStackTrace();
                }
            }
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public static Repository load(String filePath, Object root) {
        Repository repository = new Repository(filePath, root);
        repository.load();
        return repository;
    }

    public void execute(String script) throws ScriptException {
        getEngine().eval(script);
        try {
            DataOutputStream outputStream = new DataOutputStream(newOutputStream(Paths.get(filePath), CREATE, APPEND));
            outputStream.writeUTF(script);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
