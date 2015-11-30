package midmod;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Cells {
    public static Cell constant(Object value) {
        return new Cell() {
            @Override
            protected List<Object> getState() {
                return Arrays.asList(new Cell.ValueChange(value));
            }

            @Override
            public void consumeChange(Object change) {

            }
        };
    }

    public static Cell get(Object key) {
        return new Cell() {
            private Object value;

            @Override
            protected List<Object> getState() {
                return value != null ? Arrays.asList(new Cell.ValueChange(value)) : Collections.emptyList();
            }

            @Override
            public void consumeChange(Object change) {
                if(change instanceof MapCell.PutChange) {
                    MapCell.PutChange putChange = (MapCell.PutChange)change;
                    if(key.equals(putChange.getKey())) {
                        if(this.value instanceof Cell)
                            ((Cell)this.value).removeListener(this);

                        this.value = putChange.getValue();
                        sendChange(new Cell.ValueChange(putChange.getValue()));
                    }
                } else if(change instanceof MapCell.RemoveChange) {
                    MapCell.RemoveChange removeChange = (MapCell.RemoveChange)change;
                    if(key.equals(removeChange.getKey())) {
                        if(this.value instanceof Cell)
                            ((Cell)this.value).removeListener(this);

                        this.value = null;
                        sendChange(new Cell.NullChange());
                    }
                } else if(change instanceof Cell.ValueChange) {
                    Cell.ValueChange valueChange = (Cell.ValueChange)change;

                    ((MapCell)valueChange.getValue()).addListener(this);
                }
            }
        };
    }

    public static Cell put(Object key) {
        return new Cell() {
            private Object value;

            @Override
            protected List<Object> getState() {
                return value != null ? Arrays.asList(new MapCell.PutChange(key, value)) : Collections.emptyList();
            }

            @Override
            public void consumeChange(Object change) {
                if(change instanceof Cell.ValueChange) {
                    Cell.ValueChange valueChange = (Cell.ValueChange)change;
                    this.value = valueChange.getValue();
                    sendChange(new MapCell.PutChange(key, this.value));
                } else if(change instanceof Cell.NullChange) {
                    this.value = null;
                    sendChange(new MapCell.RemoveChange(key));
                }
            }
        };
    }

    public static <T, R> Cell reduce(Cell cell1, Cell cell2, Class<T> t, Class<R> r, BiFunction<T, R, Object> function) {
        return reduce(Arrays.asList(cell1, cell2), args -> function.apply((T)args[0], (R)args[1]));
    }

    public static <T, R> Cell reduce(Cell cell1, Class<T> t, Function<T, Object> function) {
        return reduce(Arrays.asList(cell1), args -> function.apply((T)args[0]));
    }

    public static <T, R> Cell reduce(List<Cell> cells, Class<T> t, Function<List<T>, Object> function) {
        return reduce(cells, args -> function.apply((List<T>)Arrays.asList(args)));
    }

    public static Cell reduce(List<Cell> cells, Function<Object[], Object> function) {
        return new Cell() {
            private Object[] arguments = new Object[cells.size()];
            private int argumentCount;

            {
                IntStream.range(0, cells.size()).forEach(i -> {
                    cells.get(i).addListener(new CellListener() {
                        @Override
                        public void consumeChange(Object change) {
                            if(change instanceof Cell.ValueChange)
                                setArgument(i, ((Cell.ValueChange)change).getValue());
                            else if(change instanceof Cell.NullChange) {
                                setArgument(i, null);
                            }
                        }
                    });
                });
            }

            private void setArgument(int i, Object value) {
                int preArgumentCount = argumentCount;

                if(arguments[i] == null && value != null)
                    argumentCount++;
                else if(arguments[i] != null && value == null)
                    argumentCount--;

                arguments[i] = value;

                if(argumentCount == cells.size()) {
                    sendChange(new Cell.ValueChange(reduce()));
                } else if(preArgumentCount == cells.size() && argumentCount < cells.size()) {
                    sendChange(new Cell.NullChange());
                }
            }

            private Object reduce() {
                return function.apply(arguments);
            }

            @Override
            protected List<Object> getState() {
                return argumentCount == cells.size() ? Arrays.asList(new Cell.ValueChange(reduce())) : Collections.emptyList();
            }

            @Override
            public void consumeChange(Object change) {

            }
        };
    }
}
