package jorch;

import java.util.List;
import java.util.Map;

public class SequentialStep implements Step {
    private List<Step> sequence;

    public SequentialStep(List<Step> sequence) {
        this.sequence = sequence;
    }

    @Override
    public void perform(Token token, Map<String, Object> context) {
        perform(token, context, 0);
    }

    private void perform(Token token, Map<String, Object> context, int index) {
        if(index == sequence.size()) {
            token.moveNext();
        } else {
            token.perform(context, sequence.get(index), new Step() {
                @Override
                public void perform(Token token, Map<String, Object> context) {
                    SequentialStep.this.perform(token, context, index + 1);
                }
            });
        }
    }

    @Override
    public String toString() {
        return sequence.toString();
    }
}
