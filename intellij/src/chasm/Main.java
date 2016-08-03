package chasm;

import java.util.*;
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
        });
    }
}
