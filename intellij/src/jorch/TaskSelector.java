package jorch;

import java.io.Serializable;

public class TaskSelector implements Serializable {
    private static final long serialVersionUID = 8182489642480615639L;

    private String name;
    private Object[] arguments;

    public TaskSelector(String name, Object[] arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
