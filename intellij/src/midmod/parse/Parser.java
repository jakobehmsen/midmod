package midmod.parse;

public interface Parser<T, R> {
    boolean parse(Input<T> input, Output<R> output);

    default boolean tryParse(Input<T> input, Output<R> output) {
        input.begin();
        output.begin();

        if(parse(input, output)) {
            input.commit();
            output.commit();

            return true;
        } else {
            input.rollback();
            output.rollback();

            return false;
        }
    }

    default Parser<T, R> then(Parser<T, R> next) {
        return (input, output) -> this.parse(input, output) && next.parse(input, output);
    }

    default Parser<T, R> or(Parser<T, R> other) {
        return (input, output) -> {
            if(this.tryParse(input, output))
                return true;
            return other.parse(input, output);
        };
    }

    default Parser<T, R> until(Parser<T, R> event) {
        return Parsers.until(this, event);
    }

    default Parser<T, R> repeat() {
        return Parsers.repeat(this);
    }

    default Parser<T, R> copy() {
        return Parsers.copy(this);
    }

    default Parser<T, R> skip() {
        return Parsers.skip(this);
    }

    default Parser<T, R> not() {
        return (input, output) -> {
            input.begin();
            output.begin();
            if(parse(input, output)) {
                input.rollback();
                output.rollback();
                return false;
            } else {
                input.commit();
                output.commit();
                return true;
            }
        };
    }
}
