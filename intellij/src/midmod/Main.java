package midmod;

public class Main {
    public static void main(String[] args) {
        Container classModel = new Container(
            new Container(
                new StringModel("name"),
                new StringModel("MyClass")
            ),
            new Container(
                new StringModel("fields"),
                new Container(
                    new Container(
                        new Container(
                            new StringModel("name"),
                            new StringModel("id")
                        ),
                        new Container(
                            new StringModel("type"),
                            new StringModel("int")
                        )
                    )
                )
            ),
            new Container(
                new StringModel("methods"),
                new Container(
                    new Container(
                        new Container(
                            new StringModel("name"),
                            new StringModel("myMethod")
                        ),
                        new Container(
                            new StringModel("returnType"),
                            new StringModel("int")
                        ),
                        new Container(
                            new StringModel("parameters"),
                            new Container(
                                new StringModel("parameters"),
                                new StringModel("int")
                            )
                        )
                    )
                )
            )
        );

        /*
        From reference models, project applications

        Could some sort of "algebraic collection oriented script language" be used?

        body => (
            is Binary

            ~.lhs | body
            ~ >> (
                is Add " + "
                | is Sub " - "
                | is Mul " * "
                | is Div " / "
                | is Rem " % "
            )
            ~.rhs | body
        )

        """public class $@.name {
            $(@.fields >>
                (^.modifier >> ~0 "public" | ~1 "private") " " @.type " " @.name
            )
            $(@.methods |
                (@.modifier >> ~0 "public" | ~1 "private") " " @.returnType " " @.name "(" ")" "{"
                    @.body >> body
                "}"
            )
        }"""

        */

        MapCell dict1 = new MapCell();
        MapCell dict1_1 = new MapCell();

        dict1.put("Composite", dict1_1);
        dict1_1.put("X", "someValue");

        MapCell dict2 = new MapCell();

        //dict1.addListener(dict2);

        dict1.put("Name", "MyClass");

        //dict1.put("FirstName", "John");
        //dict1.put("LastName", "Johnson");

        //dict1.addListener(Cells.get("Name").addListener(Cells.func((String x) -> x + "Extended").addListener(Cells.put("NameX").addListener(dict2))));

        dict1.addListener(Cells.get("Composite").addListener(Cells.get("X").addListener(Cells.put("CX").addListener(dict2))));

        System.out.println("dict1:");
        System.out.println(dict1);
        System.out.println("dict2:");
        System.out.println(dict2);

        dict1.remove("Name");

        //((DictionaryCell)dict1.get("Composite")).put("X", "SomeNewValue");


        //dict1_1.put("X", "someValue2");

        Cells.reduce(dict1.withListener(Cells.get("FirstName")), dict1.withListener(Cells.get("LastName")), String.class, String.class, (x, y) -> x + " " + y).addListener(Cells.put("FullName").addListener(dict2));

        dict1.put("Composite", dict1_1);

        System.out.println("dict1:");
        System.out.println(dict1);
        System.out.println("dict2:");
        System.out.println(dict2);

        classModel.filter(StringModel.class, new Container(
            new StringModel("name"),
            new CaptureModel()
        )).concat(classModel.filter(Container.class, new Container(
            new StringModel("fields"),
            new CaptureModel()
        ))).concat(classModel.filter(Container.class, new Container(
            new StringModel("methods"),
            new CaptureModel()
        ))).forAll((name, fields, methods) -> {
            System.out.println("public class " + name + " {");
            fields.forEach((Container f) -> {
                f.filter(StringModel.class, new Container(
                    new StringModel("name"),
                    new CaptureModel()
                )).concat(f.filter(StringModel.class, new Container(
                    new StringModel("type"),
                    new CaptureModel()
                ))).forAll((fName, fType) -> {
                    System.out.println("private " + fType + " " + fName + ";");
                });
            });
            System.out.println("}");
        });
    }
}
