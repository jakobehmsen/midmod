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
        private String typeName;

        private FieldInfo(String name, String accessModifier, String typeName) {
            this.name = name;
            this.accessModifier = accessModifier;
            this.typeName = typeName;
        }

        public String getName() {
            return name;
        }

        public String getAccessModifier() {
            return accessModifier;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    public static class MethodInfo {
        private String name;
        private String accessModifier;
        private String returnTypeName;
        private String parameters;
        private String body;

        public MethodInfo(String name, String accessModifier, String returnTypeName, String parameters, String body) {
            this.name = name;
            this.accessModifier = accessModifier;
            this.returnTypeName = returnTypeName;
            this.parameters = parameters;
            this.body = body;
        }

        public String getName() {
            return name;
        }

        public String getAccessModifier() {
            return accessModifier;
        }

        public String getReturnTypeName() {
            return returnTypeName;
        }

        public String getParameters() {
            return parameters;
        }

        public String getBody() {
            return body;
        }
    }

    private Layer layer;

    public ClassResource(Layer layer, String name) {
        this.layer = layer;
        this.name = name;
    }

    @Override
    public Resource getParent() {
        return layer;
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
                ArrayList<String> lines = new ArrayList<>();

                lines.add(m.accessModifier + " " + m.returnTypeName + " " + m.name + "(" + m.parameters + ") {");
                lines.addAll(Arrays.asList(m.body.split("\n")).stream().map(x -> "    " + x).collect(Collectors.toList()));
                lines.add("}");

                return lines;
            }

            private void updateText() {
                StringBuilder text = new StringBuilder();

                text.append("public class " + name + " {\n");

                if(c.fields.size() > 0)
                    text.append(c.fields.stream().map(x -> x.accessModifier + " " + x.typeName + " " + x.name + ";").collect(Collectors.joining("\n    ", "    ", "\n")));
                if(c.methods.size() > 0)
                    text.append(c.methods.stream().map(x ->
                        formatMethod(x).stream().collect(Collectors.joining("\n    ", "", ""))).collect(Collectors.joining("\n    ", "    ", "\n")));

                text.append("}");

                textPane.setText(text.toString());
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

    public ClassResource copy(Layer layer) {
        ClassResource copy = new ClassResource(layer, name);

        copy.fields.addAll(fields.stream().map(x -> new FieldInfo(x.name, x.accessModifier, x.typeName)).collect(Collectors.toList()));
        copy.methods.addAll(methods.stream().map(x -> new MethodInfo(x.name, x.accessModifier, x.returnTypeName, x.parameters, x.body)).collect(Collectors.toList()));

        return copy;
    }

    public void updateFrom(ClassResource newClass) {
        fields.clear();
        fields.addAll(newClass.fields.stream().map(x -> new FieldInfo(x.name, x.accessModifier, x.typeName)).collect(Collectors.toList()));
        observers.forEach(x -> x.update());
    }
}
