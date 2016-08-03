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

        ArrayList<ChangeStatement> statements = new ArrayList<>();

        ChangeStatement statement1 = new SlotAssignChangeExpression(new SlotAccessChangeExpression(new ThisChangeExpression(), new SpecificIdChangeExpression("types")), new SpecificIdChangeExpression("person"), new ObjectLiteralChangeExpression(Arrays.asList(
            new ObjectLiteralChangeExpression.Slot("fields", new ArrayChangeExpression(Arrays.asList(
                new ObjectLiteralChangeExpression(Arrays.asList(
                    new ObjectLiteralChangeExpression.Slot("name", new ObjectChangeExpression("field1")),
                    new ObjectLiteralChangeExpression.Slot("type", new ObjectLiteralChangeExpression(Arrays.asList(
                        new ObjectLiteralChangeExpression.Slot("name", new ObjectChangeExpression("int"))
                    )))
                )),
                new ObjectLiteralChangeExpression(Arrays.asList(
                    new ObjectLiteralChangeExpression.Slot("name", new ObjectChangeExpression("field2")),
                    new ObjectLiteralChangeExpression.Slot("type", new ObjectLiteralChangeExpression(Arrays.asList(
                        new ObjectLiteralChangeExpression.Slot("name", new ObjectChangeExpression("int"))
                    )))
                )),
                new ObjectLiteralChangeExpression(Arrays.asList(
                    new ObjectLiteralChangeExpression.Slot("name", new ObjectChangeExpression("field3")),
                    new ObjectLiteralChangeExpression.Slot("type", new ObjectLiteralChangeExpression(Arrays.asList(
                        new ObjectLiteralChangeExpression.Slot("name", new ObjectChangeExpression("int"))
                    )))
                ))
            )))
        )));

        statements.add(statement1);

        ChangeStatement statement2 = new CollectionAddChangeStatement(new SlotAccessChangeExpression(new SlotAccessChangeExpression(new ThisChangeExpression(), new SpecificIdChangeExpression("types")), new SpecificIdChangeExpression("person")), new SpecificIdChangeExpression("fields"), new ObjectLiteralChangeExpression(Arrays.asList(
            new ObjectLiteralChangeExpression.Slot("name", new ObjectChangeExpression("field4")),
            new ObjectLiteralChangeExpression.Slot("type", new ObjectLiteralChangeExpression(Arrays.asList(
                new ObjectLiteralChangeExpression.Slot("name", new ObjectChangeExpression("int"))
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

        Hashtable<ChangeStatement, Consumer<Map<String, List<Object>>>> patternActions = new Hashtable<>();

        // types.@typeName = {fields: [{name: @fieldName, type: {name: @fieldTypeName}} @fields]}
        ChangeStatement pattern1 = new SlotAssignChangeExpression(new SlotAccessChangeExpression(new ThisChangeExpression(), new SpecificIdChangeExpression("types")), new CaptureIdExpression("typeName"), new ObjectLiteralChangeExpression(Arrays.asList(
            new ObjectLiteralChangeExpression.Slot("fields", new TemplateArrayChangeExpression(new ClosedCaptureChangeExpression(new ObjectLiteralChangeExpression(Arrays.asList(
                new ObjectLiteralChangeExpression.Slot("name", new CaptureChangeExpression("fieldName")),
                new ObjectLiteralChangeExpression.Slot("type", new ObjectLiteralChangeExpression(Arrays.asList(
                    new ObjectLiteralChangeExpression.Slot("name", new CaptureChangeExpression("fieldTypeName"))
                )))
            )), "fields")))
        )));

        patternActions.put(pattern1, captures -> {
            System.out.println("CREATE TABLE " + captures.get("typeName").get(0) + "(" +
                captures.get("fields").stream().map(x ->
                    ((ObjectLiteralChangeExpression)x).get("fieldName").toString() + " " +
                        ((ObjectLiteralChangeExpression)x).get("fieldTypeName").toString()
                ).collect(Collectors.joining(", ")) + ")");
        });


        // types.@typeName.fields += {name: @fieldName, type: {name: @fieldTypeName}}
        ChangeStatement pattern2 = new CollectionAddChangeStatement(new SlotAccessChangeExpression(new SlotAccessChangeExpression(new ThisChangeExpression(), new SpecificIdChangeExpression("types")), new CaptureIdExpression("typeName")), new SpecificIdChangeExpression("fields"), new ObjectLiteralChangeExpression(Arrays.asList(
            new ObjectLiteralChangeExpression.Slot("name", new CaptureChangeExpression("fieldName")),
            new ObjectLiteralChangeExpression.Slot("type", new ObjectLiteralChangeExpression(Arrays.asList(
                new ObjectLiteralChangeExpression.Slot("name", new CaptureChangeExpression("fieldTypeName"))
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
