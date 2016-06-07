package paidia;

public interface ValueView {
    String getText(TextContext textContext);

    void setText(String text);

    void setup(PlaygroundView playgroundView);
}
