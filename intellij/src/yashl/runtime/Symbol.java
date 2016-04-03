package yashl.runtime;

import java.util.Hashtable;

public class Symbol {
    private static Hashtable<String, Symbol> codeToSymbolMap = new Hashtable<>();
    private int code;
    private String name;

    public Symbol(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Symbol get(String name) {
        return codeToSymbolMap.computeIfAbsent(name, n -> new Symbol(codeToSymbolMap.size(), name));
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
