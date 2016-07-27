package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class FrameValue extends AbstractValue2 {
    public static class NewSlotChange extends Change {
        private String id;

        public NewSlotChange(Value2 source, String id) {
            super(source);
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    private static class Slot extends AbstractValue2 implements Value2Observer, ValueHolderInterface {
        private FrameValue frame;
        private String id;
        //private Point location;
        private Value2 value;

        private Slot(FrameValue frame, String id, Point location, Value2 value) {
            this(frame, id, location, value, true);
        }

        private Slot(FrameValue frame, String id, Point location, Value2 value, boolean initialize) {
            this.frame = frame;
            this.id = id;
            setMetaValue("Location", location);
            //this.location = location;
            this.value = value;

            if(initialize) {
                initialize();
            }
        }

        public void initialize() {
            getValue().addObserver(this);
        }

        @Override
        public ViewBinding2 toView(PlaygroundView playgroundView) {
            Value2ViewWrapper value2ViewWrapper = new Value2ViewWrapper(playgroundView, this, getValue().toView(playgroundView).getComponent());

            playgroundView.makeEditableByMouse(value2ViewWrapper);
            value2ViewWrapper.setLocation((Point) getMetaValue("Location"));
            //value2ViewWrapper.setLocation(getLocation());

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

        @Override
        public void setValue(Value2 value) {
            getValue().removeObserver(this);

            this.value = value;
            getValue().addObserver(this);
            sendUpdated(new ValueHolderInterface.HeldValueChange(Slot.this));
            frame.sendUpdated();
        }

        /*@Override
        public void setLocation(Point location) {
            this.location = location;

            sendUpdated(new ValueHolderInterface.HeldLocationChange(Slot.this));
            frame.sendUpdated();
        }

        @Override
        public Point getLocation() {
            return location;
        }*/

        private Hashtable<String, Object> metaValues = new Hashtable<>();

        @Override
        public void setMetaValue(String id, Object value) {
            metaValues.put(id, value);
            sendUpdated(new MetaValueChange(this, id));
        }

        @Override
        public Object getMetaValue(String id) {
            return metaValues.get(id);
        }

        @Override
        public Set<String> getMetaValueIds() {
            return metaValues.keySet();
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
        public void updated(Change change) {
            sendUpdated(change);
        }

        @Override
        public Value2 shadowed(List<FrameValue> frames) {
            FrameValue frameWithSlot = frames.stream().filter(x -> x.hasSlot(id)).findFirst().get();

            // Find frames with slot; bind to that slot
            return frameWithSlot.getSlot(id);
        }
    }

    private boolean hasSlot(String id) {
        return slots.containsKey(id);
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
        sendUpdated(new NewSlotChange(this, slotId));
        return slot;
    }

    public void addSlot(Slot slot) {
        slots.put(slot.id, slot);
        sendSlotUpdated(slot.id);
    }

    private void sendSlotUpdated(String id) {
        sendUpdated(new NewSlotChange(this, id));
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

        ComponentUtil.addObserverCleanupLogic(this, view, new Value2Observer() {
            @Override
            public void updated(Change change) {
                if(change instanceof NewSlotChange) {
                    String id = ((NewSlotChange)change).getId();
                    JComponent slotView = getSlot(id).toView(playgroundView).getComponent();
                    view.add(slotView);
                }
            }
        });

        ComponentUtil.addObserverCleanupLogic(this, view, (Change change) -> {
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
        FrameValue derivedFrame = new FrameValue(idProvider.forNewFrame());

        deriveFrame(this, derivedFrame, slot -> {
            // Should be derived value?
            // Should be editable value?
            return slot.reduce();
        });

        return derivedFrame;
    }

    @Override
    public Value2 derive() {
        /*FrameValue derivedFrame = new FrameValue(idProvider.forNewFrame());

        deriveFrame(this, derivedFrame, slot -> {
            // Should be derived value?
            // Should be editable value?
            return slot.getValue().shadowed(derivedFrame);
        });

        return derivedFrame;*/

        return shadowed(Arrays.asList());
    }

    @Override
    public Value2 shadowed(java.util.List<FrameValue> frames) {
        FrameValue derivedFrame = new FrameValue(idProvider.forNewFrame());

        List<FrameValue> newFrames = new ArrayList<>();

        newFrames.addAll(frames);
        newFrames.add(derivedFrame);

        deriveFrame(this, derivedFrame, slot -> {
            // Should be derived value?
            // Should be editable value?
            return slot.getValue().shadowed(newFrames);
        });

        return derivedFrame;
    }

    private static void deriveFrame(FrameValue parent, FrameValue child, Function<Slot, Value2> valueRelation) {
        parent.addObserver(new Value2Observer() {
            @Override
            public void updated(Change change) {
                if(change instanceof NewSlotChange) {
                    ParentChildSlotRelation parentChildSlotRelation = deriveNewSlot(parent, child, ((NewSlotChange)change).getId(), valueRelation);
                    Slot childSlot = child.getSlot(((NewSlotChange)change).getId());
                    Slot parentSlot = parent.getSlot(((NewSlotChange)change).getId());

                    parentChildSlotRelation.suspend();
                    childSlot.setValue(valueRelation.apply(parentSlot));
                    parentChildSlotRelation.resume();
                }
            }
        });

        Hashtable<String, ParentChildSlotRelation> parentChildSlotMap = new Hashtable<>();

        parent.slots.entrySet().forEach(x -> {
            ParentChildSlotRelation parentChildSlotRelation = deriveNewSlot(parent, child, x.getKey(), valueRelation);
            parentChildSlotMap.put(x.getKey(), parentChildSlotRelation);
        });

        child.slots.entrySet().forEach(x -> {
            ParentChildSlotRelation parentChildSlotRelation = parentChildSlotMap.get(x.getKey());
            Slot parentSlot = parent.slots.get(x.getKey());

            parentChildSlotRelation.suspend();
            x.getValue().setValue(valueRelation.apply(parentSlot));
            parentChildSlotRelation.resume();
        });
    }

    private void declareSlot(String id) {
        slots.put(id, new Slot(this, id, null, null, false));
    }

    private static class ParentChildSlot {
        //private boolean hasLocation;
        private HashSet<String> heldMetaValueIds = new HashSet<>();
        private boolean hasValue;
    }

    private interface ParentChildSlotRelation {
        void suspend();
        void resume();
    }

    private static ParentChildSlotRelation deriveNewSlot(FrameValue parent, FrameValue child, String id, Function<Slot, Value2> valueRelation) {
        Slot parentSlot = parent.getSlot(id);
        //Slot childSlot = new Slot(child, id, parentSlot.getLocation(), parentSlot.getValue());
        Slot childSlot = new Slot(child, id, (Point) parentSlot.getMetaValue("Location"), parentSlot.getValue());
        parentSlot.getMetaValueIds().forEach(mvid ->
            childSlot.setMetaValue(mvid, parentSlot.getMetaValue(mvid)));
        child.addSlot(childSlot);

        ParentChildSlot parentChildSlot = new ParentChildSlot();

        Value2Observer childObserver = new Value2Observer() {
            @Override
            public void updated(Change change) {
                if(change instanceof ValueHolderInterface.HeldValueChange) {
                    parentChildSlot.hasValue = true;
                } else if(change instanceof ValueHolderInterface.MetaValueChange) {
                    parentChildSlot.heldMetaValueIds.add(((ValueHolderInterface.MetaValueChange)change).getId());
                }/* else if(change instanceof ValueHolderInterface.HeldLocationChange) {
                    parentChildSlot.hasLocation = true;
                }*/
            }
        };

        parentSlot.addObserver(new Value2Observer() {
            @Override
            public void updated(Change change) {
                if(change instanceof ValueHolderInterface.HeldValueChange) {
                    if(!parentChildSlot.hasValue) {
                        childSlot.removeObserver(childObserver);
                        Value2 value = valueRelation.apply(parentSlot);
                        childSlot.setValue(value);
                        childSlot.addObserver(childObserver);
                    }
                } else if(change instanceof ValueHolderInterface.MetaValueChange) {
                    String mvid = ((ValueHolderInterface.MetaValueChange)change).getId();
                    if(!parentChildSlot.heldMetaValueIds.contains(mvid)) {
                        childSlot.removeObserver(childObserver);
                        childSlot.setMetaValue(mvid, parentSlot.getMetaValue(mvid));
                        //childSlot.setLocation(parentSlot.getLocation());
                        childSlot.addObserver(childObserver);
                    }
                }/* else if(change instanceof ValueHolderInterface.HeldLocationChange) {
                    if(!parentChildSlot.hasLocation) {
                        childSlot.removeObserver(childObserver);
                        childSlot.setLocation(parentSlot.getLocation());
                        childSlot.addObserver(childObserver);
                    }
                }*/
            }
        });

        childSlot.addObserver(childObserver);

        return new ParentChildSlotRelation() {
            @Override
            public void suspend() {
                childSlot.removeObserver(childObserver);
            }

            @Override
            public void resume() {
                childSlot.addObserver(childObserver);
            }
        };
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
    public void drop(PlaygroundView playgroundView, Value2ViewWrapper sourceDroppedValue, Value2 droppedValue, Point location, Value2ViewWrapper value2ViewWrapper) {
        JComponent targetComponent = value2ViewWrapper.getView();
        Point targetLocation = location;

        Slot slot = newSlot(targetLocation, droppedValue);

        // Code duplicate from PlaygroundView
        // Location changes should be kept local?
        sourceDroppedValue.getValueHolder().addObserver(new Value2Observer() {
            @Override
            public void updated(Change change) {
                if(change instanceof ValueHolderInterface.MetaValueChange) {
                    String mvid = ((ValueHolderInterface.MetaValueChange)change).getId();
                    slot.setMetaValue(mvid, sourceDroppedValue.getValueHolder().getMetaValue(mvid));
                }
            }
        });

        /*
        sourceDroppedValue.getValueHolder().getMetaValueIds().forEach(mvid ->
            slot.setMetaValue(mvid, sourceDroppedValue.getValueHolder().getMetaValue(mvid)));
        */

        // Should be all meta values? Except location?
        if(sourceDroppedValue.getValueHolder().getMetaValue("Size") != null)
            slot.setMetaValue("Size", sourceDroppedValue.getValueHolder().getMetaValue("Size"));
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
