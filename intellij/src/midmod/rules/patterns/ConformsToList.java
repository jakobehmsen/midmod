package midmod.rules.patterns;

import midmod.pal.Consumable;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ConformsToList implements Pattern {
    private List<Pattern> list;

    public ConformsToList(List<Pattern> list) {
        this.list = list;
    }

    @Override
    public boolean matches(Consumable value, Map<String, Object> captures) {
        if(value instanceof List) {
            List<Object> otherList = (List<Object>) value;
            return IntStream.range(0, list.size())
                .allMatch(i -> list.get(i).matches(Consumable.Util.wrap(otherList.get(i)), captures));
        }

        return false;
    }
}
