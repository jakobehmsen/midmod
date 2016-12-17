package jorch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class Resolver {
    private ProcedureList procedureList = new ProcedureList(Arrays.asList(new Procedure("Procedure 1"), new Procedure("Procedure 2")));

    public ProcedureList getProcedureList() {
        return procedureList;
    }

    private Test1 test1 = new Test1("Some name", Arrays.asList("Niels", "Hugo", "Birte"));

    public Test1 getTest1() {
        return test1;
    }

    private Test2 test2 = new Test2("Some name again");

    public Test2 getTest2() {
        return test2;
    }

    public Object resolve(String target, String action, List<String> args) {
        try {
            Object targetObj = getClass().getMethod("get" + target).invoke(this);

            if(action != null) {
                Method method = Arrays.asList(targetObj.getClass().getMethods()).stream().filter(x -> x.getName().equals(action)).findFirst().get();
                Object[] argsAsArray = args.toArray();

                method.invoke(targetObj, argsAsArray);
                return targetObj;
            } else {
                return targetObj;
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }
}
