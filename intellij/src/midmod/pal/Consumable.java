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
                return wrap((List<Object>)obj);
            return wrap(Arrays.asList(obj));
        }

        public static Consumable wrap(List<Object> list) {
            return new Consumable() {
                int index;

                @Override
                public Object peek() {
                    return list.get(index);
                }

                @Override
                public void consume() {
                    index++;
                }

                @Override
                public boolean atEnd() {
                    return index >= list.size();
                }
            };
        }
    }
}
