package reo.runtime;

import javax.swing.*;

public class ComponentDictionary extends Dictionary {
    private JComponent component;

    public ComponentDictionary(Observable prototypeObservable, JComponent component) {
        super(prototypeObservable);
        this.component = component;

        get("backgroundColor").addObserver(new Observer() {
            @Override
            public void handle(Object value) {

            }
        });
    }

    public JComponent getComponent() {
        return component;
    }

    public ComponentDictionary cloneBasic() {
        try {
            return new ComponentDictionary(getPrototypeObservable(), component.getClass().newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
