package chasm;

import chasm.changelang.Parser;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public abstract class ReflectiveAspectSession implements AspectSession {
    private ModelAspect modelAspect;

    public ReflectiveAspectSession() {
        modelAspect = new ModelAspect();

        Arrays.asList(getClass().getDeclaredMethods()).stream().filter(x -> x.getAnnotation(When.class) != null).forEach(x -> {
            When when = x.getAnnotation(When.class);
            ChangeStatement pattern = Parser.parse(when.value()).get(0);

            modelAspect.when(pattern, captures -> {
                Object[] arguments = new Object[captures.declarationOrder().size()];
                for(int i = 0; i < captures.declarationOrder().size(); i++) {
                    String captureId = captures.declarationOrder().get(i);
                    arguments[i] = captures.get(captureId).buildValue();
                }
                try {
                    x.invoke(this, arguments);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                // Convert to compatible value; i.e. String, int, etc.
            });
        });
    }

    @Override
    public void processNext(ChangeStatement element) {
        modelAspect.process(element);
    }
}
