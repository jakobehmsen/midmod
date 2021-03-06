package paidia;

public class DefaultTextContext implements TextContext {
    @Override
    public String getText(TextContext textContext, String text) {
        return text;
    }

    @Override
    public String getTextAdd(String text) {
        return text;
    }

    @Override
    public String getTextMul(String text) {
        return text;
    }

    @Override
    public String getTextRaise(String text) {
        return text;
    }

    @Override
    public String getTextOperator(String text, String operator, int precedence) {
        return text;
    }
}
