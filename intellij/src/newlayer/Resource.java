package newlayer;

import javax.swing.*;

public interface Resource {
    String getName();
    JComponent toView();
    Resource getParent();
    default String getPath() {
        if(getParent() != null)
            return getParent().getPath() + "/" + getName();
        return getName();
    }
}
