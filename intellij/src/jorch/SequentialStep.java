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
            token.moveOut();
        } else {
            token.moveInto(sequence.get(index), new Step() {
                @Override
                public void perform(Token token) {
                    SequentialStep.this.perform(token, index + 1);
                }
            });
        }
    }
}
