package paidia;

import javax.swing.*;

public interface Value {
    void bindTo(Parameter parameter);
    void unbind();
    ViewBinding toComponent();
}
