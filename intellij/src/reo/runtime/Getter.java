package reo.runtime;

import javax.swing.*;

public interface Getter {
    void toView(ViewAdapter viewAdapter);

    void remove();
}
