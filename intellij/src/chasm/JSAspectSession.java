package chasm;

import chasm.changelang.Parser;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JSAspectSession implements AspectSession {
    private ModelAspect modelAspect;
    private JSObject jsAspectSession;

    public JSAspectSession(String jsAspectSrc) {
        modelAspect = new ModelAspect();
        System.out.println("jsAspectSrc:\n" + jsAspectSrc);

        try {
            ScriptEngineManager engineManager = new ScriptEngineManager();
            NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
            jsAspectSession = (JSObject) engine.compile(jsAspectSrc).eval();
        } catch (ScriptException e) {
            e.printStackTrace();
        }

        jsAspectSession.keySet().stream().filter(x -> x.startsWith("&")).forEach(x -> {
            ScriptObjectMirror action = (ScriptObjectMirror) jsAspectSession.getMember(x);
            String patternSrc = x.substring(1);
            ChangeStatement pattern = Parser.parse(patternSrc).get(0);

            modelAspect.when(pattern, captures -> {
                Object[] arguments = new Object[captures.declarationOrder().size()];
                for(int i = 0; i < captures.declarationOrder().size(); i++) {
                    String captureId = captures.declarationOrder().get(i);
                    arguments[i] = captures.get(captureId).buildValue();
                }

                action.call(jsAspectSession, arguments);
            });
        });
    }

    @Override
    public void processNext(ChangeStatement element) {
        modelAspect.process(element);
    }

    @Override
    public void close() {

    }
}
