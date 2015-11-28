package midmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class Container extends Model {
    private List<Model> models = new ArrayList<>();

    public Container() {
        this(new ArrayList<>());
    }

    public Container(Model... models) {
        this(Arrays.asList(models));
    }

    public Container(List<Model> models) {
        this.models = models;
    }

    public void addModel(Model model) {
        models.add(model);
    }

    public void removeModel(Model model) {
        models.remove(model);
    }

    public Model getModel(int index) {
        return models.get(index);
    }

    public Container filter(Pattern pattern) {
        return filter(pattern, new Container());
    }

    public <T extends Model> Tuple1<T> filter(Class<T> c1, Pattern pattern) {
        return filter(pattern, new Tuple1<T>());
    }

    public <T extends Model, R extends Model> Tuple2<T, R> filter(Class<T> c1, Class<R> c2, Pattern pattern) {
        return filter(pattern, new Tuple2<T, R>());
    }

    public <T extends Model, R extends Model, S extends Model> Tuple3<T, R, S> filter(Class<T> c1, Class<R> c2, Class<S> c3, Pattern pattern) {
        return filter(pattern, new Tuple3<T, R, S>());
    }

    public <T extends Container> T filter(Pattern pattern, T output) {
        for(Model m: models)
            pattern.filter(m, output);

        return output;
    }

    public <T extends Model> void forEach(Consumer<T> visitor) {
        for(Model m: models)
            visitor.accept((T)m);
    }

    public <T> void export(T target, BiConsumer<T, Model> exporter) {
        for(Model m: models)
            exporter.accept(target, m);
    }

    @Override
    public boolean filter(Model otherModel, Container output) {
        if(otherModel instanceof Container) {
            Container otherContainer = (Container)otherModel;

            return models.size() == otherContainer.models.size() &&
                IntStream.range(0, models.size()).allMatch(i ->
                    models.get(i).filter(otherContainer.models.get(i), output));
        }

        return false;
    }

    public <T extends Model> T single() {
        return (T)models.get(0);
    }

    public Container concat(Container tail) {
        ArrayList<Model> result = new ArrayList<>();

        result.addAll(models);
        result.addAll(tail.models);

        return new Container(result);
    }
}
