package paidia;

import javax.swing.*;

public interface Value {
    ViewBinding toComponent();
    String toSource();
    void addUsage(Usage usage);
    void removeUsage(Usage usage);
    default Runnable bind(Usage usage) {
        addUsage(usage);

        return () -> removeUsage(usage);
    }
}
