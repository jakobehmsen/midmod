package chasm;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MappedChangeProcessor {
    private static class Mapping {
        private Class<?> c;
        private Consumer<Change> processor;

        private Mapping(Class<?> c, Consumer<Change> processor) {
            this.c = c;
            this.processor = processor;
        }
    }

    private ArrayList<Mapping> mappings = new ArrayList<>();

    public <T extends Change> void addProcessor(Class<T> c, Consumer<T> processor) {
        mappings.add(new Mapping(c, change -> processor.accept((T)change)));
    }

    public void process(Change change) {
        Mapping mapping = mappings.stream().filter(x -> x.c.isInstance(change)).findFirst().get();
        mapping.processor.accept(change);
    }
}
