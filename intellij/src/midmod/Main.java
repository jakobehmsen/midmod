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

        DictionaryCell dict1 = new DictionaryCell();

        DictionaryCell dict2 = new DictionaryCell();

        //dict1.addListener(dict2);

        dict1.put("Name", "MyClass");



        dict1.addListener(Cells.get("Name").addListener(Cells.func((String x) -> x + "Extended").addListener(Cells.put("NameX").addListener(dict2))));


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
