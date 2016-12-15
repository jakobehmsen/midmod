package jorch;

import java.util.ArrayList;
import java.util.List;

public class Test1 {
    private String name;
    private List<String> children;

    public Test1(String name, List<String> children) {
        this.name = name;
        this.children = new ArrayList<>(children);
    }

    public String getName() {
        return name;
    }

    public List<String> getChildren() {
        return children;
    }

    public void addChild(String name) {
        children.add(name);
    }
}
