package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class FrameValue extends AbstractValue2 {
    private interface FrameValueObserver extends Value2Observer {
        default void updated() { }

        void addedSlot(String id);
    }

    private static class Slot extends AbstractValue2 implements Value2Observer, ValueHolderInterface {
        private interface SlotObserver extends Value2Observer {
            default void updated() { }

            void setValue();
        }

        private FrameValue frame;
        private String id;
        private Point location;
        private Value2 value;
        //private ArrayList<Runnable> prototypeSlotUpdated = new ArrayList<>();

        private Slot(FrameValue frame, String id, Point location, Value2 value) {
            this.frame = frame;
            this.id = id;
            this.location = location;
            this.value = value;

            if(value == null)
                frame.prototype.getSlot(id).addObserver(slotUpdatedObserver);

            getValue().addObserver(this);
        }

        @Override
        public ViewBinding2 toView(PlaygroundView playgroundView) {
            Value2ViewWrapper value2ViewWrapper = new Value2ViewWrapper(this, getValue().toView(playgroundView).getComponent());

            addObserver(new SlotObserver() {
                @Override
                public void setValue() {
                    value2ViewWrapper.removeAll();
                    JComponent valueView = getValue().toView(playgroundView).getComponent();
                    value2ViewWrapper.add(valueView);
                    value2ViewWrapper.setView(valueView);
                    value2ViewWrapper.revalidate();
                    value2ViewWrapper.repaint();
                }
            });

            /*ComponentUtil.addObserverCleanupLogic(this, value2ViewWrapper, () -> {
                value2ViewWrapper.setView(getValue().toView(playgroundView).getComponent());
                value2ViewWrapper.setValue(getValue());
            });*/

            playgroundView.makeEditableByMouse(value2ViewWrapper);
            value2ViewWrapper.setLocation(getLocation());

            return new ViewBinding2() {
                @Override
                public JComponent getComponent() {
                    return value2ViewWrapper;
                }
            };
        }

        @Override
        public Value2 getValue() {
            return value != null ? value : frame.prototype.getSlot(id).getValue();
        }

        private SlotObserver slotUpdatedObserver = new SlotObserver() {
            @Override
            public void setValue() {
                sendUpdatedFor(SlotObserver.class, x -> x.setValue());
            }
        };

        @Override
        public void setValue(Value2 value) {
            getValue().removeObserver(this);

            if(frame.prototype != null) {
                // If defining local value for slot
                if (this.value == null && value != null)
                    frame.prototype.getSlot(id).removeObserver(slotUpdatedObserver);
                // Else if undefining local value for slot
                else if (this.value != null && value == null)
                    frame.prototype.getSlot(id).addObserver(slotUpdatedObserver);
            }

            this.value = value;
            getValue().addObserver(this);
            sendUpdated();

            sendUpdatedFor(SlotObserver.class, x -> x.setValue());
            //frame.sendUpdated();
        }

        private Point getLocation() {
            return location != null ? location : frame.prototype.getSlot(id).getLocation();
        }

        @Override
        public String getText() {
            return getValue().getText();
        }

        @Override
        public String getSource(TextContext textContext) {
            return getValue().getSource(textContext);
        }

        @Override
        public Value2 reduce(Map<String, Value2> environment) {
            return getValue().reduce(environment);
        }

        @Override
        public void updated() {
            sendUpdated();
        }
    }

    private Slot getSlot(String id) {
        return slots.computeIfAbsent(id, x -> new Slot(this, id, null, null));
    }

    private IdProvider idProvider;
    private FrameValue prototype;
    private Hashtable<String, Slot> slots = new Hashtable<>();

    public FrameValue(FrameValue prototype, IdProvider idProvider) {
        this.prototype = prototype;
        this.idProvider = idProvider;
    }

    public Slot newSlot(Point location, Value2 value) {
        String slotId = idProvider.nextId();
        Slot slot = new Slot(this, slotId, location, value);
        slots.put(slotId, slot);
        // When this is sent, then reductions are updated, that is views are constructed from scratch
        // It should be possible to just derive a frame
        sendUpdated();
        sendUpdatedFor(FrameValueObserver.class, x -> x.addedSlot(slotId));
        return slot;
    }

    public Slot addSlot(String id) {
        Slot slot = new Slot(this, id, null, null);
        slots.put(id, slot);
        sendUpdated();
        sendSlotUpdated(id);
        return slot;
    }

    private void sendSlotUpdated(String id) {
        sendUpdatedFor(FrameValueObserver.class, x -> x.addedSlot(id));
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        JPanel view = new JPanel(null);

        view.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        view.addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        view.setSize(view.getPreferredSize());
                    }
                };

                e.getChild().addComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());
            }
        });

        view.setSize(200, 150);
        view.setPreferredSize(view.getSize());

        slots.entrySet().forEach(x -> {
            JComponent slotView = x.getValue().toView(playgroundView).getComponent();
            view.add(slotView);
        });

        ComponentUtil.addObserverCleanupLogic(this, view, new FrameValueObserver() {
            @Override
            public void addedSlot(String id) {
                JComponent slotView = getSlot(id).toView(playgroundView).getComponent();
                view.add(slotView);
            }
        });

        ComponentUtil.addObserverCleanupLogic(this, view, () -> {
            view.revalidate();
            view.repaint();
        });

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return view;
            }
        };
    }

    @Override
    public String getText() {
        return "{}";
    }

    @Override
    public String getSource(TextContext textContext) {
        return "{}";
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        FrameValue derivedFrame = new FrameValue(this, idProvider.forNewFrame());

        this.slots.entrySet().forEach(x -> derivedFrame.slots.put(x.getKey(), new Slot(derivedFrame, x.getKey(), null, null)));

        this.addObserver(new FrameValueObserver() {
            @Override
            public void addedSlot(String id) {
                derivedFrame.addSlot(id);
            }
        });

        return derivedFrame;
    }

    @Override
    public Editor createEditor(PlaygroundView playgroundView, Point location, Value2ViewWrapper value2ViewWrapper) {
        JComponent targetComponent = value2ViewWrapper.getView();
        Point targetLocation = location;

        ParseContext parseContext = new ParseContext() {
            @Override
            public ParameterValue newParameter() {
                return null;
            }

            @Override
            public IdProvider newIdProviderForFrame() {
                return null;
            }
        };

        return new ParsingEditor(playgroundView, parseContext) {
            JComponent editorComponent;

            @Override
            public String getText() {
                return " ";
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                this.editorComponent = editorComponent;

                editorComponent.setLocation(targetLocation);
                editorComponent.setSize(200, 15);
                //editorComponent.setPreferredSize(editorComponent.getSize());
                //editorComponent.setSize(value2ViewWrapper.getView().getPreferredSize());

                //value2ViewWrapper.remove(value2ViewWrapper.getView());
                targetComponent.add(editorComponent);

                targetComponent.repaint();
                targetComponent.revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                targetComponent.remove(editorComponent);

                ScopeView scopeView = new ScopeView((ValueView)parsedComponent);
                scopeView.setLocation(editorComponent.getLocation());

                targetComponent.add(scopeView);

                targetComponent.repaint();
                targetComponent.revalidate();
            }

            @Override
            protected void endEdit(Value2 parsedValue) {
                targetComponent.remove(editorComponent);

                //Value2Holder value2Holder = new Value2Holder(parsedValue);
                //JComponent valueViewWrapper = value2Holder.toView(playgroundView).getComponent();// new Value2ViewWrapper(parsedValue, scopeView);

                //valueViewWrapper.setLocation(editorComponent.getLocation());

                newSlot(targetLocation, parsedValue);

                //targetComponent.add(slot.toView(playgroundView).getComponent());

                targetComponent.repaint();
                targetComponent.revalidate();
            }

            @Override
            public void cancelEdit() {
                targetComponent.remove(editorComponent);
                //value2ViewWrapper.add(value2ViewWrapper.getView());

                targetComponent.repaint();
                targetComponent.revalidate();
            }
        };
    }
}
