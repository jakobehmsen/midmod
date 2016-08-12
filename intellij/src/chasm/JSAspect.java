package chasm;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JSAspect implements Aspect {
    private ScriptObjectMirror jsAspect;

    public JSAspect(String src) throws ScriptException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
        jsAspect = (ScriptObjectMirror) engine.compile(src).eval();
    }

    @Override
    public AspectSession newSession() {
        return new JSAspectSession((ScriptObjectMirror) jsAspect.callMember("newSession"));
    }
}
