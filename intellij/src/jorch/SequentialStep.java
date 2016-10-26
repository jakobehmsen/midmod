package jorch;

import java.util.List;

public class SequentialStep implements Step {
    private List<Step> sequence;

    public SequentialStep(List<Step> sequence) {
        this.sequence = sequence;
    }

    @Override
    public void perform(Token token) {
        perform(token, 0);
    }

    private void perform(Token token, int index) {
        if(index == sequence.size()) {
            token.moveNext();
        } else {
            token.perform(sequence.get(index), new Step() {
                @Override
                public void perform(Token token) {
                    SequentialStep.this.perform(token, index + 1);
                }

                @Override
                public String toString() {
                    return sequence.get(index).toString();
                }
            });
        }
    }

    @Override
    public String toString() {
        return sequence.toString();
    }
}
