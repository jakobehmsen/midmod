package newlayer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassResource implements Resource {
    private String name;

    public static class FieldInfo {
        private String name;
        private String accessModifier;
        private String type;

        private FieldInfo(String name, String accessModifier, String type) {
            this.name = name;
            this.accessModifier = accessModifier;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getAccessModifier() {
            return accessModifier;
        }

        public String getType() {
            return type;
        }
    }
    public static class MethodInfo {
        private String name;
        private String accessModifier;
        private String returnType;
        private String parameters;
        private String body;

        public MethodInfo(String name, String accessModifier, String returnType, String parameters, String body) {
            this.name = name;
            this.accessModifier = accessModifier;
            this.returnType = returnType;
            this.parameters = parameters;
            this.body = body;
        }

        public String getName() {
            return name;
        }

        public String getAccessModifier() {
            return accessModifier;
        }

        public String getReturnType() {
            return returnType;
        }

        public String getParameters() {
            return parameters;
        }

        public String getBody() {
            return body;
        }
    }

    public ClassResource(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    private ArrayList<FieldInfo> fields = new ArrayList<>();
    private ArrayList<MethodInfo> methods = new ArrayList<>();
    private ArrayList<ClassResourceObserver> observers = new ArrayList<>();

    public void addField(String name, String accessModifier, String type) {
        fields.add(new FieldInfo(name, accessModifier, type));
    }

    public void addMethod(String name, String accessModifier, String returnType, String parameters, String body) {
        methods.add(new MethodInfo(name, accessModifier, returnType, parameters, body));
    }

    public void addObserver(ClassResourceObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ClassResourceObserver observer) {
        observers.remove(observer);
    }

    public List<FieldInfo> getFields() {
        return fields.stream().collect(Collectors.toList());
    }

    @Override
    public JComponent toView() {
        JTextPane textPane = new JTextPane();

        textPane.setEditable(false);

        addObserver(new ClassResourceObserver() {
            ClassResource c = ClassResource.this;

            {
                updateText();
            }

            private List<String> formatMethod(MethodInfo m) {
                return Arrays.asList(
                    m.accessModifier + " " + m.returnType + " " + m.name + "(" + m.parameters + ") {",
                    "    " + m.body.replace("\n", "    "),
                    "}");
            }

            private void updateText() {
                textPane.setText("public class " + name + " {\n" +
                    c.fields.stream().map(x -> x.accessModifier + " " + x.type + " " + x.name + ";").collect(Collectors.joining("\n    ", "    ", "\n")) +
                    c.methods.stream().map(x -> formatMethod(x).stream().collect(Collectors.joining("\n    ", "", ""))).collect(Collectors.joining("\n    ", "    ", "\n")) +
                    "}");
            }

            @Override
            public void update() {
                updateText();
            }
        });

        return textPane;
    }

    public JComponent toDesign() {
        return null;
    }

    public DefaultMutableTreeNode toTreeNode(DefaultMutableTreeNode parentNode, Overview overview) {
        return new DefaultMutableTreeNode(this);
    }

    @Override
    public String toString() {
        return name;
    }

    public void updateTreeNode(DefaultMutableTreeNode node) {
        node.setUserObject(this);
    }

    public ClassResource copy() {
        ClassResource copy = new ClassResource(name);

        copy.fields.addAll(fields.stream().map(x -> new FieldInfo(x.name, x.accessModifier, x.type)).collect(Collectors.toList()));

        return copy;
    }

    public void updateFrom(ClassResource newClass) {
        fields.clear();
        fields.addAll(newClass.fields.stream().map(x -> new FieldInfo(x.name, x.accessModifier, x.type)).collect(Collectors.toList()));
        observers.forEach(x -> x.update());
    }
}
