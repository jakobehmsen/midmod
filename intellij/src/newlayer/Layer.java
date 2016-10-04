package newlayer;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.NodeOperatorVisitor;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Layer implements Resource {
    private LayerPersistor layerPersistor;
    private String name;
    private List<ClassResource> classes = new ArrayList<>();
    private String source;

    public Layer(LayerPersistor layerPersistor, String name) {
        this.layerPersistor = layerPersistor;
        this.name = name;
        source = "";
    }

    public ClassResource getClass(String name) {
        return classes.stream().filter(x -> x.getName().equals(name)).findFirst().get();
    }

    private ArrayList<LayerObserver> observers = new ArrayList<>();

    public void addObserver(LayerObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(LayerObserver observer) {
        observers.remove(observer);
    }

    public void setSource(String source) throws ScriptException {
        this.source = source;
        layerPersistor.changedSource(this);
        observers.forEach(o -> o.transformationChanged(this));
    }

    public void setClasses(List<ClassResource> newClasses) {
        List<ClassResource> oldClasses = classes;
        classes = newClasses;
        for(int i = 0; i < classes.size(); i++) {
            ClassResource newClass = classes.get(i);
            int index = i;
            oldClasses.stream().filter(x -> x.getName().equals(newClass.getName())).findFirst().ifPresent(x ->
            {
                x.updateFrom(newClass);
                classes.set(index, x);
            });
        }

        observers.forEach(o -> o.outputUpdated(this));
    }

    /*public void addClass(String name) {
        classes.add(new ClassResource(this, name));
    }*/

    public JComponent toDesign() {
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public ViewBinding<JComponent> toView(Overview overview) {
        return new ViewBinding<JComponent>() {
            JTextPane textPane = new JTextPane();

            {
                textPane.setText(source);

                LayerObserver observer = new LayerObserver() {
                    @Override
                    public void outputUpdated(Layer layer) {

                    }

                    @Override
                    public void transformationChanged(Layer layer) {
                        textPane.setText(source);
                    }

                    @Override
                    public void nameChanged(Layer layer) {

                    }
                };

                javax.swing.Timer timer = new javax.swing.Timer(100, e -> {
                    try {
                        removeObserver(observer);
                        setSource(textPane.getText());
                        addObserver(observer);
                    } catch (ScriptException se) {
                        se.printStackTrace();
                    }
                });

                timer.setRepeats(false);

                textPane.getStyledDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        timeUpdate();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        timeUpdate();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        timeUpdate();
                    }

                    private void timeUpdate() {
                        timer.restart();
                    }
                });

                addObserver(observer);
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
                textPane.select(nodeInfo.getStart(), nodeInfo.getEnd());
            }
        };

        /*JTextPane textPane = new JTextPane();

        textPane.setText(source);

        LayerObserver observer = new LayerObserver() {
            @Override
            public void outputUpdated(Layer layer) {

            }

            @Override
            public void transformationChanged(Layer layer) {
                textPane.setText(source);
            }

            @Override
            public void nameChanged(Layer layer) {

            }
        };

        javax.swing.Timer timer = new javax.swing.Timer(100, e -> {
            try {
                removeObserver(observer);
                setSource(textPane.getText());
                addObserver(observer);
            } catch (ScriptException se) {
                se.printStackTrace();
            }
        });

        timer.setRepeats(false);

        textPane.getStyledDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                timeUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                timeUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                timeUpdate();
            }

            private void timeUpdate() {
                timer.restart();
            }
        });

        addObserver(observer);

        return textPane;*/
    }

    @Override
    public Resource getParent() {
        return null;
    }

    public MutableTreeNode toTreeNode(DefaultTreeModel parentNode, Overview overview) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(this);

        classes.forEach(x -> {
            treeNode.add(x.toTreeNode(treeNode, overview));
        });

        addObserver(new LayerObserver() {
            @Override
            public void outputUpdated(Layer layer) {
                Map<String, DefaultMutableTreeNode> currentClasses = ((List<DefaultMutableTreeNode>)Collections.list(treeNode.children())).stream()
                    .collect(Collectors.toMap(n -> ((ClassResource)n.getUserObject()).getName(), n -> n));

                Map<String, ClassResource> addedClasses = classes.stream().collect(Collectors.toMap(c -> c.getName(), c -> c));

                classes.forEach(c -> {
                    if(currentClasses.containsKey(c.getName())) {
                        // Update class
                        updateClass(c, (ClassResource) currentClasses.get(c.getName()).getUserObject());
                    } else {
                        // New class
                        newClass(c);
                    }
                });

                currentClasses.forEach((cName, c) -> {
                    if(!addedClasses.containsKey(cName)) {
                        // Remove class
                        removeClass((ClassResource) currentClasses.get(cName).getUserObject());
                    }
                });

                // Update order of classes

                parentNode.nodeChanged(treeNode);
            }

            @Override
            public void transformationChanged(Layer layer) {

            }

            @Override
            public void nameChanged(Layer layer) {
                parentNode.nodeChanged(treeNode);
            }

            private void updateClass(ClassResource newClass, ClassResource currentClass) {
                DefaultMutableTreeNode node = getView(currentClass);
                newClass.updateTreeNode(node);
            }

            private void newClass(ClassResource newClass) {
                parentNode.insertNodeInto(newClass.toTreeNode(treeNode, overview), treeNode, treeNode.getChildCount());
            }

            private void removeClass(ClassResource currentClass) {
                DefaultMutableTreeNode node = getView(currentClass);
                parentNode.removeNodeFromParent(node);
            }

            private DefaultMutableTreeNode getView(ClassResource currentClass) {
                return ((List<DefaultMutableTreeNode>)Collections.list(treeNode.children())).stream()
                    .filter(n -> ((ClassResource)n.getUserObject()).getName().equals(currentClass.getName()))
                    .findFirst().get();
            }
        });

        return treeNode;
    }

    @Override
    public String toString() {
        return name;
    }

    public void transform(NashornScriptEngine engine) {
        try {
            if(source != null) {
                String modifiedSource = modifiedSource(getName(), source);
                engine.eval(modifiedSource);
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public static String modifiedSource(String name, String src) {
        // From http://stackoverflow.com/questions/6511556/javascript-parser-for-java
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source   = Source.sourceFor(name, src);

        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode program = parser.parse();

        StringBuilder sb = new StringBuilder();

        /*
        ExpressionStatement
        CallNode
        LiteralNode$StringLiteralNode
        LiteralNode$NumberLiteralNode
        AccessNode
        ObjectNode
        PropertyNode
        */

        program.getBody().getStatements().forEach(s -> {
            s.accept(new NodeOperatorVisitor<LexicalContext>(new LexicalContext()) {
                /*private int depth;

                @Override
                protected boolean enterDefault(Node node) {
                    if(depth == 0)
                        sb.append("enter(" + node.getStart() + ", " + node.getFinish() + ");\n");

                    depth++;

                    return super.enterDefault(node);
                }

                @Override
                protected Node leaveDefault(Node node) {
                    depth--;

                    if(depth == 0)
                        sb.append("leave();\n");

                    return super.leaveDefault(node);
                }*/

                private int depth;

                @Override
                public boolean enterExpressionStatement(ExpressionStatement expressionStatement) {
                    /*if(depth > 0) {
                        append("function() {\n");
                        indent();
                        append("enter(" +
                            expressionStatement.getStart() + ", " +
                            expressionStatement.getFinish() + ", " +
                            expressionStatement.position() + ", " +
                            expressionStatement.getLineNumber() + ");\n");
                        append("var result = ");
                        expressionStatement.getExpression().accept(this);
                        append(";\n");
                        append("\nleave();\n");
                        append("return result\n");
                        dedent();
                        append("}");
                    } else {
                        append("enter(" +
                            expressionStatement.getStart() + ", " +
                            expressionStatement.getFinish() + ", " +
                            expressionStatement.position() + ", " +
                            expressionStatement.getLineNumber() + ");\n");
                        depth++;

                        //append(src.substring(expressionStatement.getStart(), expressionStatement.getFinish()));
                        expressionStatement.getExpression().accept(this);

                        depth--;
                        append("\nleave();\n");
                    }*/

                    /*append("enter(" +
                        expressionStatement.getStart() + ", " +
                        expressionStatement.getFinish() + ", " +
                        expressionStatement.position() + ", " +
                        expressionStatement.getLineNumber() + ");\n");*/

                    //append(src.substring(expressionStatement.getStart(), expressionStatement.getFinish()));
                    expressionStatement.getExpression().accept(this);

                    //append("\nleave();\n");

                    //return super.enterExpressionStatement(expressionStatement);

                    return false;
                }

                @Override
                public Node leaveExpressionStatement(ExpressionStatement expressionStatement) {
                    append("\nleave();\n");

                    return super.leaveExpressionStatement(expressionStatement);
                }

                @Override
                public boolean enterCallNode(CallNode callNode) {
                    if(depth == 0) {
                        append("enter(" +
                            callNode.getStart() + ", " +
                            callNode.getFinish() + ", " +
                            callNode.position() + ", " +
                            callNode.getLineNumber() + ");\n");
                    }

                    int i = 0;
                    for (Expression arg : callNode.getArgs()) {
                        append("enter(" +
                            arg.getStart() + ", " +
                            arg.getFinish() + ", " +
                            arg.position() + ", " +
                            /*arg.getLineNumber() + */ "-1);\n");
                        append("var __arg__" + depth + "__" + i + " = ");
                        depth++;
                        arg.accept(this);
                        depth--;
                        append(";");
                        append("\nleave();\n");
                        i++;
                    }

                    callNode.getFunction().accept(this);
                    append("(");
                    i = 0;
                    boolean isFirst = true;
                    for (Expression arg : callNode.getArgs()) {
                        if(!isFirst)
                            append(", ");
                        //depth++;
                        append("__arg__" + depth + "__" + i);
                        //arg.accept(this);
                        //depth--;
                        isFirst = false;
                        i++;
                    }

                    append(")");

                    if(depth == 0) {
                        append("\nleave();\n");
                    }

                    return false;
                }

                @Override
                public boolean enterAccessNode(AccessNode accessNode) {
                    accessNode.getBase().accept(this);
                    append(".");
                    append(accessNode.getProperty());

                    return false;
                }

                @Override
                public Node leaveAccessNode(AccessNode accessNode) {
                    return super.leaveAccessNode(accessNode);
                }

                @Override
                public boolean enterObjectNode(ObjectNode objectNode) {
                    append("{");
                    boolean isFirst = true;
                    for (PropertyNode propertyNode : objectNode.getElements()) {
                        if(!isFirst)
                            append(", ");
                        propertyNode.getKey().accept(this);
                        append(": ");
                        propertyNode.getValue().accept(this);
                        isFirst = false;
                    }
                    append("}");
                    return false;
                }

                @Override
                public boolean enterIdentNode(IdentNode identNode) {
                    append(identNode.getName());

                    return false;
                }

                @Override
                public boolean enterVarNode(VarNode varNode) {
                    super.enterVarNode(varNode);

                    append("var " + varNode.getName().getName() + " = ");

                    return true;
                }

                @Override
                public Node leaveVarNode(VarNode varNode) {
                    append("\n");

                    return super.leaveVarNode(varNode);
                }

                @Override
                public boolean enterLiteralNode(LiteralNode<?> literalNode) {
                    append(literalNode.toString());

                    return super.enterLiteralNode(literalNode);
                }

                @Override
                public Node leaveLiteralNode(LiteralNode<?> literalNode) {
                    return super.leaveLiteralNode(literalNode);
                }

                private int indentationDepth = 0;
                private String indentationString = "";
                private boolean atFirstColumn = true;

                private void append(String str) {
                    for(int i = 0; i < str.length(); i++) {
                        if (atFirstColumn) {
                            sb.append(indentationString);
                            atFirstColumn = false;
                        }
                        char ch = str.charAt(i);
                        sb.append(ch);
                        if(ch == '\n') {
                            atFirstColumn = true;
                        }
                    }
                }

                private void indent() {
                    indentationDepth++;
                    indentationString = IntStream.range(0, indentationDepth * 4).mapToObj(x -> " ").collect(Collectors.joining());
                }

                private void dedent() {
                    indentationDepth--;
                    indentationString = IntStream.range(0, indentationDepth * 4).mapToObj(x -> " ").collect(Collectors.joining());
                }

                @Override
                public boolean enterFunctionNode(FunctionNode functionNode) {
                    append("function");
                    if(functionNode.isNamedFunctionExpression())
                        append(functionNode.getName());
                    append("(");
                    boolean isFirst = true;
                    for (IdentNode parameter : functionNode.getParameters()) {
                        if(!isFirst)
                            append(", ");
                        append(parameter.getName());
                        isFirst = false;
                    }
                    append(") {\n");
                    indent();
                    functionNode.getBody().accept(this);
                    dedent();
                    append("\n}");

                    return false;
                }

                @Override
                public boolean enterADD(BinaryNode binaryNode) {
                    binaryNode.lhs().accept(this);
                    append(" + ");
                    binaryNode.rhs().accept(this);

                    return false;
                }
            });
        });

        return sb.toString();
    }

    public void save() {
        layerPersistor.save(this);
    }

    public String getSource() {
        return source;
    }

    public List<ClassResource> getClasses() {
        return classes;
    }

    public void setName(String name) {
        layerPersistor.changeName(this, name);
        this.name = name;

        observers.forEach(x -> x.nameChanged(this));
    }

    public void appendSource(String sourcePart) throws ScriptException {
        String separator = source.length() == 0 || source.endsWith("\n") ? "" : "\n";
        setSource(source + separator + sourcePart);
    }
}
