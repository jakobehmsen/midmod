package newlayer;

import javax.swing.*;

public interface Overview {
    void open(Resource resource);

    ViewBinding<JComponent> getView(Resource resource);
}
