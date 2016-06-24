package paidia;

import java.util.function.Function;

public class BinaryValue2 extends AbstractValue2 {
    public BinaryValue2(Text text, TextContext textOperator, Value2 lhs, Value2 rhs, Function<ValueView[], ValueView> reducer) {
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        return null;
    }
}
