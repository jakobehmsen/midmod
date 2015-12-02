package midmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class FunctionMapCell extends MapCell {
    private static class PredicateFunctionPair {
        Predicate<Object[]> argumentsPredicate;
        Function<Object[], Object> function;

        public PredicateFunctionPair(Predicate<Object[]> argumentsPredicate, Function<Object[], Object> function) {
            this.argumentsPredicate = argumentsPredicate;
            this.function = function;
        }
    }

    public void define(String name, Predicate<Object[]> argumentsPredicate, Function<Object[], Object> function) {
        List<PredicateFunctionPair> pairs = (List<PredicateFunctionPair>) get(name);

        if(pairs == null) {
            pairs = new ArrayList<>();
            pairs.add(new PredicateFunctionPair(argumentsPredicate, function));
            put(name, pairs);
        } else {
            Optional<PredicateFunctionPair> pf = pairs.stream()
                .filter(x -> x.argumentsPredicate.equals(argumentsPredicate))
                .findFirst();

            if(pf.isPresent())
                pairs.remove(pf.get());

            pairs.add(new PredicateFunctionPair(argumentsPredicate, function));
            put(name, pairs); // Put again to provoke triggering propagation
        }
    }

    public Cell reduce(String name, List<Cell> cells) {
        return new Cell() {
            List<PredicateFunctionPair> predicateFunctions;
            Cell cell;

            {
                FunctionMapCell.this.addListener(Cells.get(name).addListener(this));
            }

            private void update(List<PredicateFunctionPair> predicateFunctions) {
                if(this.predicateFunctions != null)
                    cell.getListeners().forEach(x -> cell.removeListener(x));

                if(predicateFunctions != null && predicateFunctions.size() > 0) {
                    cell = Cells.reduce(cells, args -> {
                        Optional<PredicateFunctionPair> pf = predicateFunctions.stream().filter(x ->
                            x.argumentsPredicate.test(args)).findFirst();

                        // What is there is no match?
                        return pf.get().function;
                    });
                    cell.addListener(new CellListener() {
                        @Override
                        public void consumeChange(Object change) {
                            sendChange(change);
                        }
                    });
                }

                if(this.predicateFunctions != null && predicateFunctions == null)
                    sendChange(new NullChange());

                this.predicateFunctions = predicateFunctions;
            }

            @Override
            protected List<Object> getState() {
                return cell != null ? cell.getState() : Collections.emptyList();
            }

            @Override
            public void consumeChange(Object change) {
                if(change instanceof Cell.ValueChange) {
                    update((List<PredicateFunctionPair>) ((ValueChange) change).getValue());
                } else if(change instanceof NullChange) {
                    update(null);
                }
            }
        };
    }
}
