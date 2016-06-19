package paidia;

public interface TextContext {
    String getText(TextContext textContext, String text);

    String getTextAdd(String text);

    String getTextMul(String text);

    String getTextRaise(String text);

    String getTextOperator(String text, String operator, int precedence);
}
