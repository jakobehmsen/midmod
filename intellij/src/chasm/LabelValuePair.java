package chasm;

public class LabelValuePair extends View  {
    private String text;
    private View valueView;

    public LabelValuePair(String text, View valueView) {
        this.text = text;
        this.valueView = valueView;
    }
}
