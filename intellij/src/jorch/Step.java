package jorch;

/**
 * Created by jakob on 04-10-16.
 */
public interface Step {
    void perform(Token token);
    default Step then(Step nextStep) {
        Step currentStep = this;

        return new Step() {
            @Override
            public void perform(Token token) {
                token.moveInto(currentStep, new Step() {
                    @Override
                    public void perform(Token token) {
                        token.moveInto(nextStep, new Step() {
                            @Override
                            public void perform(Token token) {
                                token.moveOut();
                            }
                        });
                    }
                });
            }

            @Override
            public String toString() {
                return currentStep + "..." + nextStep;
            }
        };
    }
}
