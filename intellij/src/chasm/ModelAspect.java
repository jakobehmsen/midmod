package chasm;

import chasm.changelang.Parser;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModelAspect {
    private Hashtable<ChangeStatement, Consumer<Captures>> patternActions = new Hashtable<>();

    public void when(ChangeStatement pattern, Consumer<Captures> action) {
        patternActions.put(pattern, action);
    }

    private void whenSrc(String patternSrc, Consumer<Captures> action) {
        when(Parser.parse(patternSrc).get(0), action);
    }

    public <T> void when(String patternSrc, Consumer<T> action) {
        whenSrc(patternSrc, captures -> {
            Iterator<String> capturedKeys = captures.keySet().iterator();
            CapturedValue captured1 = captures.get(capturedKeys.next());
            action.accept((T)captured1.buildValue());
        });
    }

    public <T, U> void when(String patternSrc, BiConsumer<T, U> action) {
        whenSrc(patternSrc, captures -> {
            Iterator<String> capturedKeys = captures.keySet().iterator();
            CapturedValue captured1 = captures.get(capturedKeys.next());
            CapturedValue captured2 = captures.get(capturedKeys.next());
            action.accept((T)captured1.buildValue(), (U)captured2.buildValue());
        });
    }

    public <T, U, V> void when(String patternSrc, TriConsumer<T, U, V> action) {
        whenSrc(patternSrc, captures -> {
            Iterator<String> capturedKeys = captures.keySet().iterator();
            CapturedValue captured1 = captures.get(capturedKeys.next());
            CapturedValue captured2 = captures.get(capturedKeys.next());
            CapturedValue captured3 = captures.get(capturedKeys.next());
            action.accept((T)captured1.buildValue(), (U)captured2.buildValue(), (V)captured3.buildValue());
        });
    }

    public void process(ChangeStatement statement) {
        patternActions.entrySet().stream().filter(x -> {
            Captures captures = new Captures();
            if(x.getKey().matches(statement, captures)) {
                x.getValue().accept(captures);

                return true;
            }

            return false;
        }).findFirst();
    }

    private Object getCaptured(List<Object> values) {
        if(values.size() == 1)
            return values.get(0);
        return values;
    }
}
