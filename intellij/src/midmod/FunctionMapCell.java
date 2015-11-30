package midmod;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class FunctionMapCell extends MapCell {
    public void define(String name, Function<Object[], Object> function) {
        put(name, function);
    }

    public Cell reduce(String name, List<Cell> cells) {
        return new Cell() {
            Function<Object[], Object> function;
            Cell cell;

            {
                FunctionMapCell.this.addListener(Cells.get(name).addListener(this));

                //update((Function<Object[], Object>) get(name));
            }

            private void update(Function<Object[], Object> function) {
                if(this.function != null)
                    cell.getListeners().forEach(x -> cell.removeListener(x));

                if(function != null) {
                    cell = Cells.reduce(cells, function);
                    cell.addListener(new CellListener() {
                        @Override
                        public void consumeChange(Object change) {
                            sendChange(change);
                        }
                    });
                }

                this.function = function;
            }

            @Override
            protected List<Object> getState() {
                return cell != null ? cell.getState() : Collections.emptyList();
            }

            @Override
            public void consumeChange(Object change) {
                if(change instanceof Cell.ValueChange) {
                    Cell.ValueChange valueChange = (Cell.ValueChange)change;
                    update((Function<Object[], Object>)((ValueChange) change).getValue());
                } else if(change instanceof NullChange) {
                    update(null);
                }
            }
        };
    }
}
