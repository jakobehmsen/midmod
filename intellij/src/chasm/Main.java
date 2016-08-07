package chasm;

import chasm.changelang.Parser;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        String srcStatements =
            "types.person = {\n" +
            "   fields: [\n" +
            "       {name: \"field1\", type: {name: \"int\"}},\n" +
            "       {name: \"field2\", type: {name: \"int\"}},\n" +
            "       {name: \"field3\", type: {name: \"int\"}}\n" +
            "   ]\n" +
            "}" +
            "\n" +
            "types.person.fields.field4 = {type: {name: \"int\"}}";
        System.out.println(srcStatements);
        List<ChangeStatement> statements = Parser.parse(srcStatements);

        Hashtable<ChangeStatement, Consumer<Map<String, List<Object>>>> patternActions = new Hashtable<>();

        patternActions.put(Parser.parse("types.@typeName = {fields: #[{name: @fieldName, type: {name: @fieldTypeName}} @*fields]}").get(0), captures -> {
            System.out.println("CREATE TABLE " + captures.get("typeName").get(0) + "(" +
                captures.get("fields").stream().map(x ->
                    ((ObjectLiteralChangeExpression)x).get("fieldName") + " " +
                        ((ObjectLiteralChangeExpression)x).get("fieldTypeName")
                ).collect(Collectors.joining(", ")) + ")");
        });

        patternActions.put(Parser.parse("types.@typeName.fields.@fieldName = {type: {name: @fieldTypeName}}").get(0), captures -> {
            System.out.println("ALTER TABLE " + captures.get("typeName").get(0) + "\n" +
                "ADD COLUMN " + captures.get("fieldName").get(0) + " " + captures.get("fieldTypeName").get(0));
        });

        System.out.println("statements:\n" + statements.stream().map(x -> x.toString()).collect(Collectors.joining("\n")));
        System.out.println("patternActions:\n" + patternActions.entrySet().stream().map(x ->
            x.getKey() + " => " + x.getValue()).collect(Collectors.joining("\n")));

        ModelAspect modelAspect = new ModelAspect();
        modelAspect.when(Parser.parse("types.@typeName = {fields: #[{name: @fieldName, type: {name: @fieldTypeName}} @*fields]}").get(0), captures -> {
            System.out.println("CREATE TABLE " + captures.get("typeName").buildValue() + "(" +
                ((List<Object>)captures.get("fields").buildValue()).stream().map(x ->
                    ((ObjectLiteralChangeExpression)x).get("fieldName") + " " +
                        ((ObjectLiteralChangeExpression)x).get("fieldTypeName")
                ).collect(Collectors.joining(", ")) + ")");
        });
        modelAspect.when(Parser.parse("types.@typeName.fields.@fieldName = {type: {name: @fieldTypeName}}").get(0), captures -> {
            System.out.println("ALTER TABLE " + captures.get("typeName").buildValue() + "\n" +
                "ADD COLUMN " + captures.get("fieldName").buildValue() + " " + captures.get("fieldTypeName").buildValue());
        });

        AspectSession s = new ReflectiveAspectSession() {
            @When("types.@typeName.fields.@fieldName = {type: {name: @fieldTypeName}}")
            public void newField(String typeName, String fieldName, String fieldTypeName) {
                typeName.toString();
            }

            @Override
            public void close() {

            }
        };

        // Wrap pattern-action into a class?

        /*
        What if something like the following could be written to replace:

        types.@typeName = {fields: #[{name: @fieldName, type: {name: @fieldTypeName}} @fields]} => {
            System.out.println("CREATE TABLE " + typeName + "(" +
                fields.stream().map(function(x) {return fieldName + " " + fieldTypeName}).collect(Collectors.joining(", ")) +
                ")")
        }

        types.@typeName.fields.@fieldName = {type: {name: @fieldTypeName}} => {
            System.out.println("ALTER TABLE " + typeName + "\n" +
                "ADD COLUMN " + fieldName + " " + fieldTypeName);
        }

        Should the side effects also be run against the pattern matching?
        Should it be possible to explicitly tell which pattern matching to use?
        */

        statements.forEach(statement -> {
            modelAspect.process(statement);

            s.processNext(statement);

            /*patternActions.entrySet().stream().filter(x -> {
                Hashtable<String, List<Object>> captures = new Hashtable<>();
                if(x.getKey().matches(statement, captures)) {
                    x.getValue().accept(captures);

                    return true;
                }

                return false;
            }).findFirst();*/
        });
    }
}
