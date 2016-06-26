package paidia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Value2 {
    ViewBinding2 toView(PlaygroundView playgroundView);

    String getText();
    String getSource(TextContext textContext);

    Value2 reduce(Map<String, Value2> environment);

    void addObserver(Value2Observer observer);
    void removeObserver(Value2Observer observer);

    default List<String> getParameters() {
        ArrayList<String> parameters = new ArrayList<>();
        appendParameters(parameters);
        return parameters;
    }

    default void appendParameters(List<String> parameters) {

    }
}
