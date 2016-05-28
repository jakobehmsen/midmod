package paidia;

import java.util.function.Consumer;

public interface Workspace {
    void construct(Value target, Parameter valueConsumer);
}
