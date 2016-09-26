package lazymade;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

public class NashornTransformerFactory {
    private NashornScriptEngine scriptEngine;

    public NashornTransformerFactory(NashornScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public Transformer createTransformer(String script) throws ScriptException {
        CompiledScript compiledScript = scriptEngine.compile(script);
        return new Transformer() {
            @Override
            public void transform(JSObject model) {
                Bindings bindings = scriptEngine.createBindings();

                bindings.put("model", model);

                try {
                    compiledScript.eval(bindings);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
