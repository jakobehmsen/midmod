package messjour;

import java.util.function.Supplier;

public interface MessageJournal {
    Object sendTo(Message message, MessageReceiver receiver);
    Object memoize(Supplier<Object> supplier);
}
