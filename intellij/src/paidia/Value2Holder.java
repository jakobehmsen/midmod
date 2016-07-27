package paidia;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Value2Holder extends AbstractValue2 implements Value2Observer, ValueHolderInterface {
    private Value2 value;

    public Value2Holder(Value2 value) {
        this.value = value;
        this.value.addObserver(this);
    }

    @Override
    public void setValue(Value2 value) {
        this.value.removeObserver(this);
        this.value = value;
        this.value.addObserver(this);
        sendUpdated(new ValueHolderInterface.HeldValueChange(this));
        //sendUpdatedFor(ValueHolderInterface.ValueHolderObserver.class, o -> o.setValue());
    }

    @Override
    public Value2 getValue() {
        return value;
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        Value2ViewWrapper value2ViewWrapper = new Value2ViewWrapper(playgroundView, this, value.toView(playgroundView).getComponent());

        playgroundView.makeEditableByMouse(value2ViewWrapper);

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return value2ViewWrapper;
            }
        };

        //return new Value2ViewWrapper(this);
    }

    @Override
    public String getText() {
        return value.getText();
    }

    @Override
    public String getSource(TextContext textContext) {
        return value.getSource(textContext);
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        return value.reduce(environment);
    }

    @Override
    public void updated(Change change) {
        // Don't forward location changes
        /*if(change instanceof ValueHolderInterface.HeldLocationChange)
            return;*/
        // Is this filter to inclusive? Should it be restricted to only location changes?
        if(change instanceof ValueHolderInterface.MetaValueChange)
            return;

        sendUpdated(change);
    }

    @Override
    public void appendParameters(List<String> parameters) {
        value.appendParameters(parameters);
    }

    @Override
    public Value2 forApplication() {
        //return new Value2Holder(value.forApplication());

        return value.forApplication();
    }

    @Override
    public Value2 shadowed(List<FrameValue> frames) {
        return value.shadowed(frames);
    }

    private Hashtable<String, Object> metaValues = new Hashtable<>();

    @Override
    public void setMetaValue(String id, Object value) {
        if(id.equals("Size")) {
            metaValues.put(id, value);
            sendUpdated(new MetaValueChange(this, id));
        }
    }

    @Override
    public Object getMetaValue(String id) {
        return metaValues.get(id);
    }

    @Override
    public Set<String> getMetaValueIds() {
        return metaValues.keySet();
    }

    /*@Override
    public void setLocation(Point location) {

    }

    @Override
    public Point getLocation() {
        return null;
    }*/
}
