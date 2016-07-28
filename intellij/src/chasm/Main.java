package chasm;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<Change> journal = Arrays.asList(
            new AddComplexType("Person"),
            new AddFieldToComplexType("Person", "name", new Field(new PrimitiveType().withAttribute("name", "int")))
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
            updateDatabaseSql.append("ALTER TABLE " + c.getName() + "\nADD COLUMN " + c.getFieldName() + " " + fieldTypeName);
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
    }
}
