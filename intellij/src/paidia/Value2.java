package paidia;

import java.util.Map;

public interface Value2 {
    ViewBinding2 toView(PlaygroundView playgroundView);

    String getText();
    String getSource(TextContext textContext);

    Value2 reduce(Map<String, Object> environment);

    void addObserver(Value2Observer observer);
    void removeObserver(Value2Observer observer);
}
