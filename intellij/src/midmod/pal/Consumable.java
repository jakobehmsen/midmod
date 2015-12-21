package midmod.pal;

import java.util.Arrays;
import java.util.List;

public interface Consumable {
    Object peek();
    void consume();
    boolean atEnd();

    class Util {
        public static Consumable wrap(Object obj) {
            if(obj instanceof List)
                return new ListConsumable((List<Object>)obj);
            return new SingletonConsumable(obj);
        }
    }
}
