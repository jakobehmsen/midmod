package chasm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        //JsonChangeStatement statement = new SlotAssign(new ThisExpression(), new SpecificIdExpression("x"), new ObjectChangeExpression(6));

        /*
        types.person = {
            fields: [
                {name: "field1", type: {name: "int"}},
                {name: "field2", type: {name: "int"}},
                {name: "field3", type: {name: "int"}}
            ]
        }

        types.person.fields +=
        */

        ArrayList<JsonChangeStatement> statements = new ArrayList<>();

        JsonChangeStatement statement1 = new SlotAssign(new SlotAccess(new ThisExpression(), new SpecificIdExpression("types")), new SpecificIdExpression("person"), new ObjectLiteralExpression(Arrays.asList(
            new ObjectLiteralExpression.Slot("fields", new ArrayChangeExpression(Arrays.asList(
                new ObjectLiteralExpression(Arrays.asList(
                    new ObjectLiteralExpression.Slot("name", new ObjectChangeExpression("field1")),
                    new ObjectLiteralExpression.Slot("type", new ObjectLiteralExpression(Arrays.asList(
                        new ObjectLiteralExpression.Slot("name", new ObjectChangeExpression("int"))
                    )))
                )),
                new ObjectLiteralExpression(Arrays.asList(
                    new ObjectLiteralExpression.Slot("name", new ObjectChangeExpression("field2")),
                    new ObjectLiteralExpression.Slot("type", new ObjectLiteralExpression(Arrays.asList(
                        new ObjectLiteralExpression.Slot("name", new ObjectChangeExpression("int"))
                    )))
                )),
                new ObjectLiteralExpression(Arrays.asList(
                    new ObjectLiteralExpression.Slot("name", new ObjectChangeExpression("field3")),
                    new ObjectLiteralExpression.Slot("type", new ObjectLiteralExpression(Arrays.asList(
                        new ObjectLiteralExpression.Slot("name", new ObjectChangeExpression("int"))
                    )))
                ))
            )))
        )));

        statements.add(statement1);

        JsonChangeStatement statement2 = new CollectionAddChangeStatement(new SlotAccess(new SlotAccess(new ThisExpression(), new SpecificIdExpression("types")), new SpecificIdExpression("person")), new SpecificIdExpression("fields"), new ObjectLiteralExpression(Arrays.asList(
            new ObjectLiteralExpression.Slot("name", new ObjectChangeExpression("field4")),
            new ObjectLiteralExpression.Slot("type", new ObjectLiteralExpression(Arrays.asList(
                new ObjectLiteralExpression.Slot("name", new ObjectChangeExpression("int"))
            )))
        )));

        statements.add(statement2);

        //JsonChangeStatement pattern = new SlotAssign(new ThisExpression(), new SpecificIdExpression("x"), new ObjectChangeExpression(6));
        //JsonChangeStatement pattern = new SlotAssign(new ThisExpression(), new SpecificIdExpression("x"), new CaptureExpression("valueOfX"));
        /*
        types.person = {
            fields: [
                {name: :fieldName, type: {name: :fieldTypeName}}
            ]
        }

        types.person = {
            fields: [{
                    name: :fieldName,
                    type: {
                        name: :fieldTypeName
                    }
                }:fields // Structure all inner captures into object literal
            ]
        }
        */

        Hashtable<JsonChangeStatement, Consumer<Map<String, List<Object>>>> patternActions = new Hashtable<>();

        // types.@typeName = {fields: [{name: @fieldName, type: {name: @fieldTypeName}} @fields]}
        JsonChangeStatement pattern1 = new SlotAssign(new SlotAccess(new ThisExpression(), new SpecificIdExpression("types")), new CaptureIdExpression("typeName"), new ObjectLiteralExpression(Arrays.asList(
            new ObjectLiteralExpression.Slot("fields", new TemplateArrayChangeExpression(new ClosedCaptureExpression(new ObjectLiteralExpression(Arrays.asList(
                new ObjectLiteralExpression.Slot("name", new CaptureExpression("fieldName")),
                new ObjectLiteralExpression.Slot("type", new ObjectLiteralExpression(Arrays.asList(
                    new ObjectLiteralExpression.Slot("name", new CaptureExpression("fieldTypeName"))
                )))
            )), "fields")))
        )));

        patternActions.put(pattern1, captures -> {
            System.out.println("CREATE TABLE " + captures.get("typeName").get(0) + "(" +
                captures.get("fields").stream().map(x ->
                    ((ObjectLiteralExpression)x).get("fieldName").toString() + " " +
                        ((ObjectLiteralExpression)x).get("fieldTypeName").toString()
                ).collect(Collectors.joining(", ")) + ")");
        });


        // types.@typeName.fields += {name: @fieldName, type: {name: @fieldTypeName}}
        JsonChangeStatement pattern2 = new CollectionAddChangeStatement(new SlotAccess(new SlotAccess(new ThisExpression(), new SpecificIdExpression("types")), new CaptureIdExpression("typeName")), new SpecificIdExpression("fields"), new ObjectLiteralExpression(Arrays.asList(
            new ObjectLiteralExpression.Slot("name", new CaptureExpression("fieldName")),
            new ObjectLiteralExpression.Slot("type", new ObjectLiteralExpression(Arrays.asList(
                new ObjectLiteralExpression.Slot("name", new CaptureExpression("fieldTypeName"))
            )))
        )));

        patternActions.put(pattern2, captures -> {
            System.out.println("ALTER TABLE " + captures.get("typeName").get(0) + "\n" +
                "ADD COLUMN " + captures.get("fieldName") + " " + captures.get("fieldTypeName"));
        });

        System.out.println("statements:\n" + statements.stream().map(x -> x.toString()).collect(Collectors.joining("\n")));
        System.out.println("patternActions:\n" + patternActions.entrySet().stream().map(x ->
            x.getKey() + " => " + x.getValue()).collect(Collectors.joining("\n")));

        statements.forEach(statement -> {
            patternActions.entrySet().stream().filter(x -> {
                Hashtable<String, List<Object>> captures = new Hashtable<>();
                if(x.getKey().matches(statement, captures)) {
                    x.getValue().accept(captures);

                    return true;
                }

                return false;
            }).findFirst();

            /*if(pattern.matches(statement, captures)) {
                System.out.println("captures:\n" + captures);

                System.out.println("CREATE TABLE " + captures.get("typeName").get(0) + "(" +
                    captures.get("fields").stream().map(x ->
                        ((ObjectLiteralExpression)x).get("fieldName").toString() + " " +
                            ((ObjectLiteralExpression)x).get("fieldTypeName").toString()
                    ).collect(Collectors.joining(", ")) + ")");
            }*/
        });

        if(1 != 2)
            return;



        List<Change> journal = Arrays.asList(
            new AddComplexType("Person"),
            new AddFieldToComplexType("Person", "name", new Field(new PrimitiveType().withAttribute("name", "string"))),
            new SetAttributeInFieldInComplexType("Person", "name", "displayName", "Name"),
            new RenameFieldInComplexType("Person", "name", "firstName"),
            new SetAttributeInFieldInComplexType("Person", "firstName", "displayName", "First Name"),
            new AddFieldToComplexType("Person", "lastName", new Field(new PrimitiveType().withAttribute("name", "string"))),
            new SetAttributeInFieldInComplexType("Person", "lastName", "displayName", "Last Name"),
            new AddFieldToComplexType("Person", "salary", new Field(new PrimitiveType().withAttribute("name", "int"))),
            new SetAttributeInFieldInComplexType("Person", "salary", "displayName", "Salary")
        );

        new Interaction(new VerticalSectionView(Arrays.asList(
            new LabelValuePair("name", new DefaultEditView(new GetFieldExpression(new GetVariableExpression("person"), "name")))
        )));

        Image image = new Image();

        journal.forEach(x -> x.perform(image));

        StringBuilder updateDatabaseSql = new StringBuilder();

        // Identify version before generation update script

        int version = 0;
        MappedChangeProcessor changeProcessor = new MappedChangeProcessor();
        changeProcessor.addProcessor(AddComplexType.class, c -> {
            updateDatabaseSql.append("CREATE TABLE " + c.getName() + "\n");
        });
        changeProcessor.addProcessor(AddFieldToComplexType.class, c -> {
            String fieldTypeName = (String) c.getField().getType().getAttribute("name");
            if(fieldTypeName.equals("string")) {
                fieldTypeName = "nvarchar(255)";
            }
            updateDatabaseSql.append("ALTER TABLE " + c.getName() + "\nADD COLUMN " + c.getFieldName() + " " + fieldTypeName + "\n");
        });
        changeProcessor.addProcessor(RenameFieldInComplexType.class, c -> {
            updateDatabaseSql.append("ALTER TABLE " + c.getTypeName() + "\nRENAME COLUMN " + c.getOldFieldName() + " TO " + c.getNewFieldName() + "\n");
        });
        changeProcessor.addProcessor(Change.class, c -> {

        });
        journal.stream().skip(version).forEach(x -> changeProcessor.process(x));

        System.out.println("UPDATE SCRIPT:");
        System.out.println(updateDatabaseSql);

        // If database doesn't already exist
        StringBuilder createDatabaseSql = new StringBuilder();
        image.getTypeNames().forEach(typeName -> {
            ComplexType type = (ComplexType) image.getType(typeName);

            String sql = "CREATE TABLE " + typeName + "(" +
                type.getFieldNames().stream().map(fieldName -> {
                    Field field = type.getField(fieldName);
                    String fieldTypeName = (String) field.getType().getAttribute("name");
                    if(fieldTypeName.equals("string")) {
                        fieldTypeName = "nvarchar(255)";
                    }
                    return fieldName + " " + fieldTypeName;
                }).collect(Collectors.joining(", ")) +
                ")";

            createDatabaseSql.append(sql + "\n");
        });

        System.out.println("CREATION SCRIPT:");
        System.out.println("CREATE DATABASE PersonCatalog");
        System.out.println("USE DATABASE PersonCatalog");
        System.out.println(createDatabaseSql);



        StringBuilder javaCode = new StringBuilder();
        image.getTypeNames().forEach(typeName -> {
            ComplexType type = (ComplexType) image.getType(typeName);

            String javaCodeClass = "public class " + typeName + " {\n" +
                type.getFieldNames().stream().map(fieldName -> {
                    Field field = type.getField(fieldName);
                    String fieldTypeName = (String) field.getType().getAttribute("name");
                    return "    private " + fieldName + " " + fieldTypeName;
                }).collect(Collectors.joining("\n")) +
                "\n}";

            javaCode.append(javaCodeClass + "\n");
        });

        System.out.println("JAVA CODE:");
        System.out.println(javaCode);

        JFrame frame = new JFrame("Person Catalog");

        JTabbedPane tabbedPane = new JTabbedPane();

        image.getTypeNames().forEach(typeName -> {
            ComplexType type = (ComplexType) image.getType(typeName);

            JPanel instances = new JPanel(new BorderLayout());

            JToolBar toolBar = new JToolBar();

            instances.add(toolBar, BorderLayout.NORTH);

            JList<Object> instancesList = new JList<>(new DefaultListModel<Object>());

            toolBar.add(new AbstractAction("New...") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JDialog dialog = new JDialog(frame, "New " + typeName);

                    dialog.getContentPane().setLayout(new BorderLayout());
                    ;
                    JPanel fieldsPanel = new JPanel(new GridLayout(0, 2));

                    fieldsPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
                    ((GridLayout)fieldsPanel.getLayout()).setHgap(3);
                    ((GridLayout)fieldsPanel.getLayout()).setVgap(3);

                    Hashtable<String, JComponent> instanceFieldValueComponents = new Hashtable<>();

                    type.getFieldNames().forEach(fieldName -> {
                        Field field = type.getField(fieldName);

                        String displayName = (String) field.getAttribute("displayName");
                        if(displayName == null)
                            displayName = fieldName;
                        JLabel label = new JLabel(displayName);
                        JComponent valueComponent = null;

                        if(field.getType().getAttribute("name").equals("int")) {
                            valueComponent = new JSpinner();
                        } else if(field.getType().getAttribute("name").equals("string")) {
                            valueComponent = new JTextField();
                            ((JTextField)valueComponent).setColumns(10);
                        }
                        fieldsPanel.add(label);
                        fieldsPanel.add(valueComponent);
                        instanceFieldValueComponents.put(fieldName, valueComponent);
                    });

                    dialog.getContentPane().add(fieldsPanel, BorderLayout.CENTER);

                    JPanel buttonPanel = new JPanel();
                    JButton buttonOK = new JButton("OK");
                    buttonOK.addActionListener(actionEvent -> {
                        Hashtable<String, Object> instanceFieldValues = new Hashtable<>();

                        type.getFieldNames().forEach(fieldName -> {

                            Field field = type.getField(fieldName);

                            Object value = null;
                            JComponent valueComponent = instanceFieldValueComponents.get(fieldName);

                            if(field.getType().getAttribute("name").equals("int")) {
                                value = ((Number)((JSpinner)valueComponent).getValue()).intValue();
                            } else if(field.getType().getAttribute("name").equals("string")) {
                                value = ((JTextField)valueComponent).getText();
                            }

                            instanceFieldValues.put(fieldName, value);
                        });

                        ((DefaultListModel<Object>)instancesList.getModel()).addElement(instanceFieldValues);

                        dialog.setVisible(false);
                    });
                    buttonPanel.add(buttonOK);
                    JButton buttonCancel = new JButton("Cancel");
                    buttonCancel.addActionListener(actionEvent -> {
                        dialog.setVisible(false);
                    });
                    buttonPanel.add(buttonCancel);
                    dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

                    dialog.pack();

                    dialog.setLocationRelativeTo(frame);

                    dialog.setVisible(true);
                }
            });

            toolBar.add(new AbstractAction("Edit...") {
                @Override
                public void actionPerformed(ActionEvent e) {

                }
            });

            toolBar.add(new AbstractAction("Delete") {
                @Override
                public void actionPerformed(ActionEvent e) {

                }
            });

            instances.add(instancesList, BorderLayout.CENTER);

            tabbedPane.add(typeName, instances);
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(tabbedPane);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
