package reo.runtime;

import java.awt.*;

public class ColorAdapter extends AbstractObservable implements Observer {
    private int r;
    private int g;
    private int b;

    private Binding rBinding;
    private Binding gBinding;
    private Binding bBinding;

    @Override
    public void handle(Object value) {
        if(rBinding != null) {
            rBinding.remove();
            gBinding.remove();
            bBinding.remove();
        }

        rBinding = ((Dictionary)value).get("r").bind(new Observer() {
            @Override
            public void handle(Object value) {
                r = (int)value;
                update();
            }
        });
        gBinding = ((Dictionary)value).get("g").bind(new Observer() {
            @Override
            public void handle(Object value) {
                g = (int)value;
                update();
            }
        });
        bBinding = ((Dictionary)value).get("b").bind(new Observer() {
            @Override
            public void handle(Object value) {
                b = (int)value;
                update();
            }
        });
    }

    @Override
    public void release() {
        sendRelease();
    }

    private void update() {
        sendChange(new Color(r, g, b));
    }

    @Override
    protected void sendStateTo(Observer observer) {
        observer.handle(new Color(r, g, b));
    }
}
