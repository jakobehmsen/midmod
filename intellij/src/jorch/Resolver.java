package jorch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Resolver {
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

                return method.invoke(targetObj, argsAsArray);
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
