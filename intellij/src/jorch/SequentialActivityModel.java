package jorch;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SequentialActivityModel implements ActivityModel {
    private List<ActivityModel> sequence;

    public SequentialActivityModel(List<ActivityModel> sequence) {
        this.sequence = sequence;
    }

    public SequentialActivityModel then(ActivityModel next) {
        return new SequentialActivityModel(Stream.concat(sequence.stream(), Arrays.asList(next).stream()).collect(Collectors.toList()));
    }

    @Override
    public Step toStep() {
        return new SequentialStep(sequence.stream().map(x -> x.toStep()).collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return sequence.toString();
    }
}
