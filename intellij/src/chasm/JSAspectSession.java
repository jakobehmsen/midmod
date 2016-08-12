package chasm;

import chasm.changelang.Parser;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JSAspectSession implements AspectSession {
    private ScriptObjectMirror jsAspectSession;
    private ModelAspect modelAspect;

    public JSAspectSession(String jsAspectSrc) throws ScriptException {
        System.out.println("jsAspectSrc:\n" + jsAspectSrc);

        ScriptEngineManager engineManager = new ScriptEngineManager();
        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
        initialize((ScriptObjectMirror) engine.compile(jsAspectSrc).eval());
    }

    public JSAspectSession(ScriptObjectMirror jsAspectSession) {
        initialize(jsAspectSession);
    }

    private void initialize(ScriptObjectMirror jsAspectSession) {
        this.jsAspectSession = jsAspectSession;
        modelAspect = new ModelAspect();

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
        jsAspectSession.callMember("close");
    }
}
