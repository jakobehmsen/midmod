package paidia;

import javax.swing.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

public class ComponentUtil {
    public static void addObserverCleanupLogic(Value2 value, JComponent view, Value2Observer observer) {
        addAttachAndCleanupLogic(view, () -> value.addObserver(observer), () -> value.removeObserver(observer));
    }

    public static void addAttachAndCleanupLogic(JComponent view, Runnable attach, Runnable cleanup) {
        view.addHierarchyListener(new HierarchyListener() {
            boolean lastDisplayable;

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if(e.getChanged() == view && view.isDisplayable() != lastDisplayable) {
                    if(!lastDisplayable) {
                        attach.run();
                    } else {
                        cleanup.run();
                    }

                    lastDisplayable = view.isDisplayable();
                }
            }
        });
    }
}
