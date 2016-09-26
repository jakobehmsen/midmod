package lazymade;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ScriptException, IOException {
        NashornScriptEngine scriptEngine = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
        NashornTransformerFactory transformerFactory = new NashornTransformerFactory(scriptEngine);

        JSObject model = (JSObject) scriptEngine.eval("({})");

        String resourceDir = Main.class.getResource("").getFile();
        String script1 = new String(java.nio.file.Files.readAllBytes(Paths.get(resourceDir, "Transformer1.js")));
        String script2 = new String(java.nio.file.Files.readAllBytes(Paths.get(resourceDir, "Transformer2.js")));

        List<Transformer> transformers = Arrays.asList(
            transformerFactory.createTransformer(script1),
            transformerFactory.createTransformer(script2)
        );

        transformers.forEach(t -> t.transform(model));

        model.toString();
    }
}
