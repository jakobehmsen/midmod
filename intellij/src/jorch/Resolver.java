package jorch;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

public class Resolver {
    private Test1 test1 = new Test1("Some name");

    public Test1 getTest1() {
        return test1;
    }

    private Test2 test2 = new Test2("Some name again");

    public Test2 getTest2() {
        return test2;
    }

    public Object resolve(String target) {
        try {
            return getClass().getMethod("get" + target).invoke(this);
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
