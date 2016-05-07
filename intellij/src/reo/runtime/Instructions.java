package reo.runtime;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class Instructions {
    public static Instruction halt() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.halt();
            }
        };
    }

    public static Instruction loadConstant(Object value) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(new Constant(value));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction pop() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().pop();

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction dup() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().dup();

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction dup2() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().dup2();

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction load(int ordinal) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().load(ordinal);

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction storeSlot(String name) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(2);

                Observable value = evaluation.getOperand(0);
                Observable dictObs = evaluation.getOperand(1);

                Observables.setSlot(dictObs, name, value);

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadSlot(String name) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Observable dictObs = evaluation.getFrame().pop();

                evaluation.getFrame().push(Observables.getSlot(dictObs, name));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction removeSlot(String name) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Observable dictObs = evaluation.getFrame().pop();

                Observables.removeSlot(dictObs, name);

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction messageSend(String selector, int arity) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(arity);
                Observable[] arguments = new Observable[arity];
                System.arraycopy(evaluation.getOperands(), 0, arguments, 0, arity);
                Observable receiverObs = evaluation.getFrame().pop();

                evaluation.getFrame().push(Observables.messageSend(evaluation.getUniverse(), receiverObs, selector, arguments));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction addi() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(2);

                Observable lhs = evaluation.getOperand(0);
                Observable rhs = evaluation.getOperand(1);

                evaluation.getFrame().push(new Reducer(Arrays.asList(lhs, rhs), new Function<Object[], Object>() {
                    @Override
                    public Object apply(Object[] objects) {
                        return (int)objects[0] + (int)objects[1];
                    }

                    @Override
                    public String toString() {
                        return "addi";
                    }
                }));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction javaInvokeInstance(java.lang.reflect.Method method) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                ArrayList<Observable> arguments = new ArrayList<>();
                evaluation.popOperands(method.getParameterCount());
                Observable receiver = evaluation.getFrame().pop();
                arguments.add(receiver);
                arguments.addAll(Arrays.asList(evaluation.getOperands()));

                evaluation.getFrame().push(new Reducer(arguments, new Function<Object[], Object>() {
                    @Override
                    public Object apply(Object[] objects) {
                        Object receiver = objects[0];
                        Object[] arguments = new Object[objects.length - 1];
                        System.arraycopy(objects, 1, arguments, 0, arguments.length);

                        try {
                            return method.invoke(receiver, arguments);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public String toString() {
                        return "javaInvoke: " + method;
                    }
                }));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction javaNewInstance(java.lang.reflect.Constructor<?> constructor) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                ArrayList<Observable> arguments = new ArrayList<>();
                evaluation.popOperands(constructor.getParameterCount());
                Observable receiver = evaluation.getFrame().pop();
                arguments.add(receiver);
                arguments.addAll(Arrays.asList(evaluation.getOperands()));

                evaluation.getFrame().push(new Reducer(arguments, new Function<Object[], Object>() {
                    @Override
                    public Object apply(Object[] objects) {
                        Object receiver = objects[0];
                        Object[] arguments = new Object[objects.length - 1];
                        System.arraycopy(objects, 1, arguments, 0, arguments.length);

                        try {
                            return constructor.newInstance(arguments);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public String toString() {
                        return "javaInvoke: " + constructor;
                    }
                }));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction wrapComponent(Class<? extends JComponent> componentClass) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                try {
                    JComponent c = componentClass.newInstance();
                    evaluation.getFrame().push(Observables.constant(new ComponentDictionary(evaluation.getUniverse().getComponentPrototype(), c)));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction newDict() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Observable prototypeObservable = evaluation.getFrame().pop();

                evaluation.getFrame().push(new Constant(new Dictionary(prototypeObservable)));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction newMethod(Behavior behavior) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(new Constant(new Method(evaluation.getUniverse(), behavior)));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadNull() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(evaluation.getUniverse().getNull());

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction storeLocal(int ordinal) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Observable value = evaluation.getFrame().pop();
                evaluation.getFrame().set(ordinal, value);

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction ret() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Frame frame = evaluation.getFrame();
                Observable result = frame.pop();
                Frame outerFrame = frame.getOuter();
                outerFrame.push(result);
                evaluation.setFrame(outerFrame);
                evaluation.getFrame().incrementIP();
            }
        };
    }
}
