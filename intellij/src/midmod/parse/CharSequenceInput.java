package midmod.parse;

import java.util.Stack;

public class CharSequenceInput implements Input<Character> {
    private CharSequence charSequence;
    private int index;

    public CharSequenceInput(CharSequence charSequence) {
        this.charSequence = charSequence;
    }

    @Override
    public void consume() {
        index++;
    }

    @Override
    public boolean peeksMatchesPeek(Input<Character> input) {
        return charSequence.charAt(index) == input.peekChar();
    }

    @Override
    public boolean atEnd() {
        return index >= charSequence.length();
    }

    @Override
    public char peekChar() {
        return charSequence.charAt(index);
    }

    @Override
    public <R> void copyTo(Output<R> output) {
        output.putChar(peekChar());
    }

    @Override
    public InputState<Character> getState() {
        return new InputState<Character>() {
            int startIndex = index;

            @Override
            public Input<Character> delta() {
                return new CharSequenceInput(charSequence.subSequence(startIndex, index));
            }
        };
    }

    @Override
    public CharSequence peekChars() {
        return charSequence.subSequence(index, charSequence.length());
    }

    private Stack<Integer> indexes = new Stack<>();

    @Override
    public void begin() {
        indexes.push(index);
    }

    @Override
    public void commit() {
        indexes.pop();
    }

    @Override
    public void rollback() {
        index = indexes.pop();
    }
}
