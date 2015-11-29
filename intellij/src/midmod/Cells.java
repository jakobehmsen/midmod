package midmod;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Cells {
    public static Cell get(Object key) {
        return new Cell() {
            private Object value;

            @Override
            protected List<Object> getState() {
                return value != null ? Arrays.asList(new Cell.ValueChange(value)) : Collections.emptyList();
            }

            @Override
            public void consumeChange(Object change) {
                if(change instanceof DictionaryCell.PutChange) {
                    DictionaryCell.PutChange putChange = (DictionaryCell.PutChange)change;
                    if(key.equals(putChange.getKey())) {
                        if(this.value instanceof Cell)
                            ((Cell)this.value).removeListener(this);

                        this.value = putChange.getValue();
                        sendChange(new Cell.ValueChange(putChange.getValue()));
                    }
                } else if(change instanceof Cell.ValueChange) {
                    Cell.ValueChange valueChange = (Cell.ValueChange)change;

                    ((Cell)valueChange.getValue()).addListener(this);
                }
            }
        };
    }

    public static Cell put(Object key) {
        return new Cell() {
            private Object value;

            @Override
            protected List<Object> getState() {
                return value != null ? Arrays.asList(new DictionaryCell.PutChange(key, value)) : Collections.emptyList();
            }

            @Override
            public void consumeChange(Object change) {
                if(change instanceof DictionaryCell.ValueChange) {
                    Cell.ValueChange valueChange = (Cell.ValueChange)change;
                    this.value = valueChange.getValue();
                    sendChange(new DictionaryCell.PutChange(key, this.value));
                }
            }
        };
    }

    public static <T, R> Cell func(Function<T, R> function) {
        return new Cell() {
            private Object value;

            @Override
            protected List<Object> getState() {
                return value != null ? Arrays.asList(new DictionaryCell.ValueChange(value)) : Collections.emptyList();
            }

            @Override
            public void consumeChange(Object change) {
                if(change instanceof DictionaryCell.ValueChange) {
                    DictionaryCell.ValueChange valueChange = (DictionaryCell.ValueChange)change;
                    this.value = function.apply((T)valueChange.getValue());
                    sendChange(new DictionaryCell.ValueChange(this.value));
                }
            }
        };
    }
}
