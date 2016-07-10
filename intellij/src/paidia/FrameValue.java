package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

public class FrameValue extends AbstractValue2 {
    private interface FrameValueObserver extends Value2Observer {
        default void updated() { }

        void addedSlot(String id);
    }

    private static class Slot extends AbstractValue2 implements Value2Observer, ValueHolderInterface {
        private FrameValue frame;
        private String id;
        private Point location;
        private Value2 value;

        private Slot(FrameValue frame, String id, Point location, Value2 value) {
            this.frame = frame;
            this.id = id;
            this.location = location;
            this.value = value;

            getValue().addObserver(this);
        }

        @Override
        public ViewBinding2 toView(PlaygroundView playgroundView) {
            Value2ViewWrapper value2ViewWrapper = new Value2ViewWrapper(playgroundView, this, getValue().toView(playgroundView).getComponent());

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
            return value;
        }

        private ValueHolderInterface.ValueHolderObserver slotUpdatedObserver = new ValueHolderInterface.ValueHolderObserver() {
            @Override
            public void updated() {
                if(value == null) {
                    sendUpdatedFor(ValueHolderInterface.ValueHolderObserver.class, o -> o.setValue());
                    sendUpdated();
                    frame.sendUpdated();
                }
            }

            @Override
            public void setValue() {
                if(value == null) {
                    sendUpdatedFor(ValueHolderInterface.ValueHolderObserver.class, o -> o.setValue());
                    sendUpdated();
                    frame.sendUpdated();
                }
            }

            @Override
            public void setLocation() {
                if(Slot.this.location == null) {
                    sendUpdatedFor(ValueHolderInterface.ValueHolderObserver.class, o -> o.setLocation());
                    sendUpdated();
                    frame.sendUpdated();
                }
            }
        };

        @Override
        public void setValue(Value2 value) {
            getValue().removeObserver(this);

            this.value = value;
            getValue().addObserver(this);
            sendUpdatedFor(ValueHolderInterface.ValueHolderObserver.class, o -> o.setValue());
            sendUpdated();
            frame.sendUpdated();
        }

        @Override
        public void setLocation(Point location) {
            this.location = location;

            sendUpdatedFor(ValueHolderInterface.ValueHolderObserver.class, o -> o.setLocation());
            sendUpdated();
            frame.sendUpdated();
        }

        public Point getLocation() {
            return location;
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

        @Override
        public Value2 shadowed(FrameValue frame) {
            return frame.getSlot(id);
        }
    }

    private Slot getSlot(String id) {
        return slots.computeIfAbsent(id, x -> new Slot(this, id, null, null));
    }

    private IdProvider idProvider;
    private Hashtable<String, Slot> slots = new Hashtable<>();

    public FrameValue(IdProvider idProvider) {
        this.idProvider = idProvider;
    }

    public Slot newSlot(Point location, Value2 value) {
        String slotId = idProvider.nextId();
        Slot slot = new Slot(this, slotId, location, value);
        slots.put(slotId, slot);
        // When this is sent, then reductions are updated, that is views are constructed from scratch
        // It should be possible to just derive a frame; i.e. a new tool should be made for this
        sendUpdated();
        sendUpdatedFor(FrameValueObserver.class, x -> x.addedSlot(slotId));
        return slot;
    }

    public void addSlot(Slot slot) {
        slots.put(slot.id, slot);
        sendUpdated();
        sendSlotUpdated(slot.id);
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

                        view.revalidate();
                        view.repaint();
                        System.out.println("Frame component resized");
                    }
                };

                e.getChild().addComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());

                view.revalidate();
                view.repaint();
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());

                view.revalidate();
                view.repaint();
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
    public Value2 reduce() {
        return reduce(new Hashtable<>());
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        /*FrameValue derivedFrame = new FrameValue(idProvider.forNewFrame());

        this.slots.entrySet().forEach(x -> derivedFrame.slots.put(x.getKey(), new Slot(derivedFrame, x.getKey(), x.getValue().getLocation(), x.getValue().getValue().reduce(environment))));

        return derivedFrame;*/

        FrameValue derivedFrame = new FrameValue(idProvider.forNewFrame());

        deriveFrame(this, derivedFrame, value -> {
            // Should be derived value?
            // Should be editable value?
            return value.reduce(environment);
        });

        return derivedFrame;
    }

    @Override
    public Value2 derive() {
        FrameValue derivedFrame = new FrameValue(idProvider.forNewFrame());

        deriveFrame(this, derivedFrame, value -> {
            // Should be derived value?
            // Should be editable value?
            return value;
        });

        return derivedFrame;
    }

    private static void deriveFrame(FrameValue parent, FrameValue child, Function<Value2, Value2> valueRelation) {
        parent.addObserver(new FrameValueObserver() {
            @Override
            public void addedSlot(String id) {
                deriveNewSlot(parent, child, id, valueRelation);
            }
        });

        parent.slots.entrySet().forEach(x -> {
            deriveNewSlot(parent, child, x.getKey(), valueRelation);
        });
    }

    private static class ParentChildSlot {
        private boolean hasLocation;
        private boolean hasValue;
    }

    private static void deriveNewSlot(FrameValue parent, FrameValue child, String id, Function<Value2, Value2> valueRelation) {
        Slot parentSlot = parent.getSlot(id);
        Slot childSlot = new Slot(child, id, parentSlot.getLocation(), parentSlot.getValue());
        child.addSlot(childSlot);

        ParentChildSlot parentChildSlot = new ParentChildSlot();

        ValueHolderInterface.ValueHolderObserver childObserver = new ValueHolderInterface.ValueHolderObserver() {
            @Override
            public void setValue() {
                parentChildSlot.hasValue = true;
            }

            @Override
            public void setLocation() {
                parentChildSlot.hasLocation = true;
            }
        };

        parentSlot.addObserver(new ValueHolderInterface.ValueHolderObserver() {
            @Override
            public void setValue() {
                if(!parentChildSlot.hasValue) {
                    childSlot.removeObserver(childObserver);
                    Value2 value = valueRelation.apply(parentSlot.getValue());
                    childSlot.setValue(value);
                    childSlot.addObserver(childObserver);
                }
            }

            @Override
            public void setLocation() {
                if(!parentChildSlot.hasLocation) {
                    childSlot.removeObserver(childObserver);
                    childSlot.setLocation(parentSlot.getLocation());
                    childSlot.addObserver(childObserver);
                }
            }
        });

        childSlot.addObserver(childObserver);
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
                return idProvider.forNewFrame();
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

                newSlot(targetLocation, parsedValue);

                targetComponent.repaint();
                targetComponent.revalidate();
            }

            @Override
            public void cancelEdit() {
                targetComponent.remove(editorComponent);

                targetComponent.repaint();
                targetComponent.revalidate();
            }
        };
    }

    @Override
    public void drop(PlaygroundView playgroundView, Value2ViewWrapper droppedComponent, Point location, Value2ViewWrapper value2ViewWrapper) {
        JComponent targetComponent = value2ViewWrapper.getView();
        Point targetLocation = location;

        newSlot(targetLocation, droppedComponent.getValue());
    }

    @Override
    public boolean canMove(Value2ViewWrapper parentViewWrapper, Value2ViewWrapper viewWrapper) {
        // TODO: When viewWrapper is moved, then update the location of the slot, such that the location can be
        // forwarded to derivations
        return true;
    }

    @Override
    public boolean canReduceFrom() {
        return true;
    }
}
