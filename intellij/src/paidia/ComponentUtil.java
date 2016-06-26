package paidia;

import javax.swing.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

public class ComponentUtil {
    public static void addObserverCleanupLogic(Value2 value, JComponent view, Value2Observer observer) {
        view.addHierarchyListener(new HierarchyListener() {
            boolean lastDisplayable;

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if(e.getChanged() == view && view.isDisplayable() != lastDisplayable) {
                    if(!lastDisplayable) {
                        value.addObserver(observer);
                    } else {
                        value.removeObserver(observer);
                    }

                    lastDisplayable = view.isDisplayable();
                }
            }
        });
    }
}
