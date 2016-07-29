package chasm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
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

            toolBar.add(new AbstractAction("New...") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JDialog dialog = new JDialog(frame, "New " + typeName);

                    dialog.getContentPane().setLayout(new GridLayout(0, 2));
                    ((JPanel)dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
                    ((GridLayout)dialog.getContentPane().getLayout()).setHgap(3);
                    ((GridLayout)dialog.getContentPane().getLayout()).setVgap(3);

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
                        dialog.getContentPane().add(label);
                        dialog.getContentPane().add(valueComponent);
                    });

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

            JList<Object> instancesList = new JList<>();

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
