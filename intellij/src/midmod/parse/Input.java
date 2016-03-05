package midmod.parse;

public interface Input<T> extends TransationSupport {
    default <R> boolean parseSequence(Input<T> input, Output<R> output) {
        while(!atEnd() && !input.atEnd() && peeksMatchesPeek(input)) {
            copyTo(output);
            consume();
            input.consume();
        }

        return input.atEnd();
    }

    default <R> boolean parseSequenceNot(Input<T> input, Output<R> output) {
        /*try {
            peeksMatchesPeek(input);
        } catch (StringIndexOutOfBoundsException e) {
            e.toString();
        }*/

        while(!atEnd() && !input.atEnd() && !peeksMatchesPeek(input)) {
            copyTo(output);
            consume();
            input.consume();
        }

        return input.atEnd();
    }


    void consume();

    boolean peeksMatchesPeek(Input<T> input);

    boolean atEnd();

    char peekChar();

    <R> void copyTo(Output<R> output);

    InputState<T> getState();

    default Input<T> forCopy() {
        return this;
    }

    CharSequence peekChars();
}
