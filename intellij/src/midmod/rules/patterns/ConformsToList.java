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
    public boolean matchesList(Consumable value, Map<String, Object> captures) {
        if(value instanceof List) {
            List<Object> otherList = (List<Object>) value;
            return IntStream.range(0, list.size())
                .allMatch(i -> list.get(i).matchesList(Consumable.Util.wrap(otherList.get(i)), captures));
        }

        return false;
    }

    @Override
    public boolean matchesSingle(Object value, Map<String, Object> captures) {
        return false;
    }
}
