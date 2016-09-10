package messjour;

import java.util.function.Supplier;

public interface MessageJournal {
    Object sendTo(Message message, MessageReceiver receiver);
    <T> T memoize(Supplier<T> supplier);
    <T> T newInstance(Supplier<T> supplier);
}
