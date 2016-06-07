package paidia;

public class Text {
    private String raw;
    private String formatted;

    public Text(String raw, String formatted) {
        this.raw = raw;
        this.formatted = formatted;
    }

    public String getRaw() {
        return raw;
    }

    public String getFormatted() {
        return formatted;
    }
}
