package newlayer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassResource extends AnnotatableResource implements Resource {
    public static class FieldInfo extends AnnotatableResource {
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

        public void render(List<String> lines) {
            getAnnotations().forEach(x -> lines.add(x.toString()));
            lines.add(accessModifier + " " + typeName + " " + name + ";");
        }

        public FieldInfo copy() {
            FieldInfo fieldInfo = new FieldInfo(name, accessModifier, typeName);
            super.copyTo(fieldInfo);
            return fieldInfo;
        }
    }

    public static class MethodInfo extends AnnotatableResource {
        private String name;
        private String accessModifier;
        private String returnTypeName;
        private List<ParameterInfo> parameters;
        private String body;

        public MethodInfo(String name, String accessModifier, String returnTypeName, List<ParameterInfo> parameters, String body) {
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

        public List<ParameterInfo> getParameters() {
            return parameters;
        }

        public String getBody() {
            return body;
        }

        public void render(List<String> lines) {
            getAnnotations().forEach(x -> lines.add(x.toString()));
            lines.add(accessModifier + " " + returnTypeName + " " + name + "(" +
                parameters.stream().map(x -> x.toString()).collect(Collectors.joining(", ")) +
                ") {");
            lines.addAll(Arrays.asList(body.split("\n")).stream().map(x -> "    " + x).collect(Collectors.toList()));
            lines.add("}");
        }

        public MethodInfo copy() {
            MethodInfo methodInfo = new MethodInfo(name, accessModifier, returnTypeName, parameters, body);
            super.copyTo(methodInfo);
            return methodInfo;
        }

        public ParameterInfo getParameter(int index) {
            return parameters.get(index);
        }
    }

    public static class ConstructorInfo extends MethodInfo {
        private ClassResource classResource;

        public ConstructorInfo(ClassResource classResource, String accessModifier, List<ParameterInfo> parameters, String body) {
            super("<init>", accessModifier, "<thisClass>", parameters, body);
            this.classResource = classResource;
        }

        public ConstructorInfo copy(ClassResource classResource) {
            ConstructorInfo constructorInfo = new ConstructorInfo(classResource, getAccessModifier(), getParameters(), getBody());
            super.copyTo(constructorInfo);
            return constructorInfo;
        }

        @Override
        public void render(List<String> lines) {
            getAnnotations().forEach(x -> lines.add(x.toString()));
            lines.add(getAccessModifier() + " " + classResource.getName() + "(" +
                getParameters().stream().map(x -> x.toString()).collect(Collectors.joining(", ")) +
                ") {");
            lines.addAll(Arrays.asList(getBody().split("\n")).stream().map(x -> "    " + x).collect(Collectors.toList()));
            lines.add("}");
        }
    }

    public static class ParameterInfo extends AnnotatableResource {
        private String typeName;
        private String name;

        public ParameterInfo(String typeName, String name) {
            this.typeName = typeName;
            this.name = name;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            String annotationsStr = getAnnotations().stream().map(x -> x.toString()).collect(Collectors.joining(" "));
            return (annotationsStr.length() > 0 ? annotationsStr + " " : "") + typeName + " " + name;
        }
    }

    private Layer layer;
    private String name;
    private String superClassName;
    private ArrayList<String> interfaceNames;

    public ClassResource(Layer layer, String name) {
        this.layer = layer;
        this.name = name;
        interfaceNames = new ArrayList<>();
    }

    @Override
    public Resource getParent() {
        return layer;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public void addInterfaceName(String interfaceName) {
        interfaceNames.add(interfaceName);
    }

    public void removeInterfaceName(String interfaceName) {
        interfaceNames.remove(interfaceName);
    }

    public List<String> getInterfaceNames() {
        return interfaceNames.stream().collect(Collectors.toList());
    }

    private ArrayList<FieldInfo> fields = new ArrayList<>();
    private ArrayList<MethodInfo> methods = new ArrayList<>();
    private ArrayList<ConstructorInfo> constructors = new ArrayList<>();
    private ArrayList<ClassResourceObserver> observers = new ArrayList<>();

    public void addField(String name, String accessModifier, String type) {
        fields.add(new FieldInfo(name, accessModifier, type));
    }

    public FieldInfo getField(String name) {
        return fields.stream().filter(x -> x.getName().equals(name)).findFirst().get();
    }

    public List<FieldInfo> getFields() {
        return fields.stream().collect(Collectors.toList());
    }

    public void addMethod(String name, String accessModifier, String returnType, List<ParameterInfo> parameters, String body) {
        methods.add(new MethodInfo(name, accessModifier, returnType, parameters, body));
    }

    public MethodInfo getMethod(String name) {
        return methods.stream().filter(x -> x.getName().equals(name)).findFirst().get();
    }

    public List<MethodInfo> getMethods() {
        return methods.stream().collect(Collectors.toList());
    }

    public void addConstructor(String accessModifier, List<ParameterInfo> parameters, String body) {
        constructors.add(new ConstructorInfo(this, accessModifier, parameters, body));
    }

    public ConstructorInfo getConstructor(List<String> parameterTypeNames) {
        return constructors.stream()
            .filter(x -> x.getParameters().stream().map(p -> p.getTypeName()).collect(Collectors.toList()).equals(parameterTypeNames))
            .findFirst().get();
    }

    public List<ConstructorInfo> getConstructors() {
        return constructors.stream().collect(Collectors.toList());
    }

    public void addObserver(ClassResourceObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ClassResourceObserver observer) {
        observers.remove(observer);
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

            private void updateText() {
                StringBuilder text = new StringBuilder();

                getAnnotations().forEach(x -> text.append(x.toString() + "\n"));
                text.append("public class " + name);
                if(superClassName != null)
                    text.append(" extends " + superClassName);
                if(interfaceNames.size() > 0)
                    text.append(" implements " + interfaceNames.stream().collect(Collectors.joining(", ")));
                text.append(" {" + "\n");

                ArrayList<String> members = new ArrayList<>();
                c.fields.forEach(f -> f.render(members));
                c.constructors.forEach(c -> c.render(members));
                c.methods.forEach(m -> m.render(members));
                text.append(members.stream().collect(Collectors.joining("\n    ", "    ", "\n")));

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

        copy.updateFrom(this);

        return copy;
    }

    public void updateFrom(ClassResource newClass) {
        superClassName = newClass.superClassName;
        interfaceNames.clear();
        interfaceNames.addAll(newClass.interfaceNames);
        fields.clear();
        fields.addAll(newClass.fields.stream().map(x -> x.copy()).collect(Collectors.toList()));
        constructors.clear();
        constructors.addAll(newClass.constructors.stream().map(x -> x.copy(this)).collect(Collectors.toList()));
        methods.clear();
        methods.addAll(newClass.methods.stream().map(x -> x.copy()).collect(Collectors.toList()));
        observers.forEach(x -> x.update());
    }
}
