package newlayer;

import com.sun.glass.events.KeyEvent;

import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassResource extends AnnotatableResource implements Resource {
    public static class FieldInfo extends AnnotatableResource {
        private ResourceNodeInfo resourceNodeInfo;
        private String name;
        private String accessModifier;
        private String typeName;

        public FieldInfo(ResourceNodeInfo resourceNodeInfo, String name, String accessModifier, String typeName) {
            this.resourceNodeInfo = resourceNodeInfo;
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
            FieldInfo fieldInfo = new FieldInfo(resourceNodeInfo, name, accessModifier, typeName);
            super.copyTo(fieldInfo);
            return fieldInfo;
        }

        public JComponent toView(Overview overview) {
            String text = getAccessModifier() + " " + getTypeName() + " " + getName() + ";";
            JComponent headerViewContent = createLinkText(text, resourceNodeInfo, overview);
            return leftAligned(headerViewContent);
        }

        public ModelNode toModelNode(Overview overview) {
            String text = getAccessModifier() + " " + getTypeName() + " " + getName() + ";";
            return new HorizontalModelNode(Arrays.asList(new TextModelNode(text)));
        }
    }

    private static JComponent leftAligned(JComponent component) {
        Box view = Box.createHorizontalBox();
        view.add(component);
        view.add(Box.createHorizontalGlue());
        return view;
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

        public JComponent toView(Overview overview) {
            Box view = Box.createVerticalBox();

            view.add(leftAligned(new JLabel(accessModifier + " " + returnTypeName + " " + name + "(" +
                parameters.stream().map(x -> x.toString()).collect(Collectors.joining(", ")) +
                ") {")));

            Box bodyView = Box.createHorizontalBox();
            bodyView.add(Box.createRigidArea(new Dimension(20, 0)));
            JTextPane bodyContentView = new JTextPane() {
                @Override
                public Dimension getMaximumSize() {
                    return getPreferredSize();
                }

                @Override
                public Insets getInsets() {
                    return new Insets(0, 0, 0, 0);
                }
            };
            bodyContentView.setOpaque(false);
            bodyContentView.setText(body);
            bodyContentView.setEditable(false);
            bodyView.add(bodyContentView);
            bodyView.add(Box.createHorizontalGlue());

            view.add(bodyView);

            view.add(leftAligned(new JLabel("}")));

            return view;
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

        @Override
        public JComponent toView(Overview overview) {
            Box view = Box.createVerticalBox();

            view.add(leftAligned(new JLabel(getAccessModifier() + " " + classResource.getName() + "(" +
                getParameters().stream().map(x -> x.toString()).collect(Collectors.joining(", ")) +
                ") {")));

            Box bodyView = Box.createHorizontalBox();
            bodyView.add(Box.createRigidArea(new Dimension(20, 0)));
            JTextPane bodyContentView = new JTextPane() {
                @Override
                public Dimension getMaximumSize() {
                    return getPreferredSize();
                }

                @Override
                public Insets getInsets() {
                    return new Insets(0, 0, 0, 0);
                }
            };
            bodyContentView.setOpaque(false);
            bodyContentView.setText(getBody());
            bodyContentView.setEditable(false);
            bodyView.add(bodyContentView);
            bodyView.add(Box.createHorizontalGlue());

            view.add(bodyView);

            view.add(leftAligned(new JLabel("}")));

            return view;
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

    private ResourceNodeInfo resourceNodeInfo;
    private String name;
    private String superClassName;
    private ArrayList<String> interfaceNames;

    public ClassResource(ResourceNodeInfo resourceNodeInfo, String name) {
        this.resourceNodeInfo = resourceNodeInfo;
        this.name = name;
        interfaceNames = new ArrayList<>();
    }

    @Override
    public Resource getParent() {
        return resourceNodeInfo.getResource();
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

    public void addField(FieldInfo fieldInfo) {
        fields.add(fieldInfo);
    }

    /*public void addField(String name, String accessModifier, String type) {
        fields.add(new FieldInfo(name, accessModifier, type));
    }*/

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

    private static JComponent createLinkText(String text, ResourceNodeInfo resourceNodeInfo, Overview overview) {
        JButton linkText = new JButton(text) {
            @Override
            public Insets getInsets() {
                return new Insets(0, 0, 0, 0);
            }
        };
        linkText.setToolTipText(resourceNodeInfo.getResource().getPath());
        linkText.setContentAreaFilled(false);
        linkText.setBorderPainted(false);
        linkText.setFocusPainted(false);
        linkText.setOpaque(true);
        linkText.getModel().addChangeListener(new ChangeListener() {
            private Color bgColor;
            @Override
            public void stateChanged(ChangeEvent e) {
                ButtonModel model = (ButtonModel) e.getSource();
                if (model.isRollover()) {
                    bgColor = linkText.getBackground();
                    linkText.setBackground(Color.CYAN);
                } else {
                    linkText.setBackground(bgColor);
                    bgColor = null;
                }
            }
        });
        linkText.addActionListener(e -> {
            resourceNodeInfo.open(overview);
        });
        return linkText;
    }

    @Override
    public ViewBinding<JComponent> toView(Overview overview) {
        ViewBinding<JComponent> viewBinding = new ViewBinding<JComponent>() {
            JPanel view = new JPanel();
            JComponent view2 = new JPanel();

            {
                VerticalModelNode classNode = new VerticalModelNode();
                HorizontalModelNode classHeaderNode = new HorizontalModelNode();

                classHeaderNode.add(new TextModelNode("public class " + name));
                if(superClassName != null)
                    classHeaderNode.add(new TextModelNode("extends " + superClassName));
                if(interfaceNames.size() > 0)
                    classHeaderNode.add(new TextModelNode("implements " + interfaceNames.stream().collect(Collectors.joining(", "))));
                classHeaderNode.add(new TextModelNode("{"));

                classNode.add(classHeaderNode);

                AreaModelNode classMembersAreaNode = new AreaModelNode();
                VerticalModelNode classMembersAreaNodeContent = new VerticalModelNode();
                classMembersAreaNodeContent.dontAddGlue();

                fields.forEach(x -> {
                    classMembersAreaNodeContent.add(x.toModelNode(overview));
                });

                classMembersAreaNode.add(classMembersAreaNodeContent);

                classNode.add(classMembersAreaNode);

                classNode.add(new HorizontalModelNode(Arrays.asList(new TextModelNode("}"))));

                if(1 != 2) {
                    view2 = classNode.buildComponent();
                }



                view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
                //view.setBackground(Color.RED);

                // Build annotations view
                //getAnnotations().forEach(x -> text.append(x.toString() + "\n"));
                StringBuilder header = new StringBuilder();
                header.append("public class " + name);
                if(superClassName != null)
                    header.append(" extends " + superClassName);
                if(interfaceNames.size() > 0)
                    header.append(" implements " + interfaceNames.stream().collect(Collectors.joining(", ")));
                header.append(" {" + "\n");


                Box headerView = Box.createHorizontalBox();
                JComponent headerViewContent = createLinkText(header.toString(), resourceNodeInfo, overview);

                //headerView.add(new JLabel(header.toString()));
                headerView.add(headerViewContent);
                headerView.add(Box.createHorizontalGlue());
                view.add(headerView);

                Box membersViewLine = Box.createHorizontalBox();
                membersViewLine.setBackground(Color.BLUE);
                membersViewLine.add(Box.createRigidArea(new Dimension(20, 0)));
                //membersViewLine.add(Box.createHorizontalStrut(20));

                Box membersViewColumn = Box.createVerticalBox();

                membersViewColumn.setBackground(Color.GREEN);
                
                fields.forEach(f -> {
                    membersViewColumn.add(f.toView(overview));
                });

                constructors.forEach(c -> {
                    membersViewColumn.add(c.toView(overview));
                });

                methods.forEach(m -> {
                    membersViewColumn.add(m.toView(overview));
                });
                
                //membersViewColumn.add(new JLabel("private int x;"));
                //membersViewColumn.add(new JLabel("private int y;"));
                //membersViewColumn.add(Box.createVerticalStrut(0));

                membersViewLine.add(membersViewColumn);
                //membersViewLine.add(new JLabel("test"));
                membersViewLine.add(Box.createHorizontalGlue());

                view.add(membersViewLine);
                //view.add(Box.createVerticalGlue());

                Box footerView = Box.createHorizontalBox();
                footerView.add(new JLabel("}"));
                footerView.add(Box.createHorizontalGlue());
                view.add(footerView);
                view.add(Box.createVerticalGlue());
            }

            @Override
            public JComponent getView() {
                return view;
            }

            @Override
            public void remove() {

            }

            @Override
            public void select(NodeInfo nodeInfo) {
                // Is this relevant to support?
            }
        };

        if(1 != 2)
            return viewBinding;

        return new ViewBinding<JComponent>() {
            private boolean isCreating;
            private JTextPane textPane = new JTextPane();
            private boolean isUpdating;
            private int memberCreationStart;
            private int editStart;
            private int editEnd;

            {
                JPopupMenu popupMenu = new JPopupMenu();

                popupMenu.add(new AbstractAction("Add field") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String indention = "    ";
                        try {
                            isUpdating = true;
                            textPane.getDocument().insertString(memberCreationStart, indention + "\n", null);
                            isUpdating = false;
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                        isCreating = true;
                        textPane.requestFocusInWindow();
                        textPane.setEditable(true);
                        textPane.setCaretPosition(memberCreationStart + indention.length());
                        editStart = textPane.getCaretPosition();
                        editEnd = editStart;
                    }
                });

                textPane.setComponentPopupMenu(popupMenu);

                textPane.setDocument(new DefaultStyledDocument() {
                    @Override
                    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                        if(isUpdating) {
                            super.insertString(offs, str, a);
                            return;
                        }

                        if(isCreating) {
                            editEnd = Math.max(editEnd, offs + str.length());

                            SimpleAttributeSet style = new SimpleAttributeSet();
                            StyleConstants.setBold(style, true);
                            //StyleConstants.setBackground(style, Color.BLUE);
                            //StyleConstants.setForeground(style, Color.WHITE);

                            super.insertString(offs, str, style);
                        }
                    }

                    @Override
                    public void remove(int offs, int len) throws BadLocationException {
                        if(isUpdating) {
                            super.remove(offs, len);
                            return;
                        }

                        if(isCreating) {
                            super.remove(offs, len);
                            editEnd -= len;
                        }
                    }
                });

                textPane.registerKeyboardAction(e -> {
                    if(isCreating) {
                        isCreating = false;
                        try {
                            String creationString = textPane.getDocument().getText(editStart, editEnd - editStart);
                            textPane.getDocument().remove(editStart, editEnd - editStart);
                            String[] parts = creationString.trim().split(" ");
                            String accessModifier = parts[0];
                            String typeName = parts[1];
                            String name = parts[2].substring(0, parts[2].length() - 1);
                            textPane.setEditable(false);
                            try {
                                Layer layer = (Layer)resourceNodeInfo.getResource();
                                layer.appendSource("getClass('" + ClassResource.this.name + "').addField('" + name + "', '" + accessModifier + "', '" + typeName + "')");
                            } catch (ScriptException e1) {
                                e1.printStackTrace();
                            }
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                    }
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), 0);

                /*JPopupMenu popupMenu = new JPopupMenu();

                popupMenu.add(new AbstractAction("Add field") {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                    }
                });

                textPane.setComponentPopupMenu(popupMenu);*/

                textPane.setEditable(false);

                addObserver(new ClassResourceObserver() {
                    ClassResource c = ClassResource.this;

                    {
                        updateText();
                    }

                    private void updateText() {
                        isUpdating = true;

                        StringBuilder text = new StringBuilder();

                        getAnnotations().forEach(x -> text.append(x.toString() + "\n"));
                        text.append("public class " + name);
                        if(superClassName != null)
                            text.append(" extends " + superClassName);
                        if(interfaceNames.size() > 0)
                            text.append(" implements " + interfaceNames.stream().collect(Collectors.joining(", ")));
                        text.append(" {" + "\n");

                        memberCreationStart = text.length();

                        ArrayList<String> members = new ArrayList<>();
                        c.fields.forEach(f -> f.render(members));
                        c.constructors.forEach(c -> c.render(members));
                        c.methods.forEach(m -> m.render(members));
                        text.append(members.stream().collect(Collectors.joining("\n    ", "    ", "\n")));

                        text.append("}");

                        textPane.setText("");
                        try {
                            textPane.getStyledDocument().insertString(0, text.toString(), null);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                        //textPane.setText(text.toString());

                        isUpdating = false;
                    }

                    @Override
                    public void update() {
                        updateText();
                    }
                });
            }

            @Override
            public JComponent getView() {
                return textPane;
            }

            @Override
            public void remove() {

            }

            @Override
            public void select(NodeInfo nodeInfo) {

            }
        };

        /*JTextPane textPane = new JTextPane();

        JPopupMenu popupMenu = new JPopupMenu();

        popupMenu.add(new AbstractAction("Add field") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        textPane.setComponentPopupMenu(popupMenu);

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

        return textPane;*/
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
        ClassResource copy = new ClassResource(resourceNodeInfo, name);

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
