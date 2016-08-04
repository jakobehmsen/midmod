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

        patternActions.put(Parser.parse("types.@typeName = {fields: #[{name: @fieldName, type: {name: @fieldTypeName}} @fields]}").get(0), captures -> {
            System.out.println("CREATE TABLE " + captures.get("typeName").get(0) + "(" +
                captures.get("fields").stream().map(x ->
                    ((ObjectLiteralChangeExpression)x).get("fieldName").toString() + " " +
                        ((ObjectLiteralChangeExpression)x).get("fieldTypeName").toString()
                ).collect(Collectors.joining(", ")) + ")");
        });

        patternActions.put(Parser.parse("types.@typeName.fields.@fieldName = {type: {name: @fieldTypeName}}").get(0), captures -> {
            System.out.println("ALTER TABLE " + captures.get("typeName").get(0) + "\n" +
                "ADD COLUMN " + captures.get("fieldName") + " " + captures.get("fieldTypeName"));
        });

        System.out.println("statements:\n" + statements.stream().map(x -> x.toString()).collect(Collectors.joining("\n")));
        System.out.println("patternActions:\n" + patternActions.entrySet().stream().map(x ->
            x.getKey() + " => " + x.getValue()).collect(Collectors.joining("\n")));

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
        */

        statements.forEach(statement -> {
            patternActions.entrySet().stream().filter(x -> {
                Hashtable<String, List<Object>> captures = new Hashtable<>();
                if(x.getKey().matches(statement, captures)) {
                    x.getValue().accept(captures);

                    return true;
                }

                return false;
            }).findFirst();
        });
    }
}
