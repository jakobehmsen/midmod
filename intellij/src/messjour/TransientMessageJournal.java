package messjour;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Supplier;

public class TransientMessageJournal implements MessageJournal {
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<Object> memoizations = new ArrayList<>();

    @Override
    public Object sendTo(Message message, MessageReceiver receiver) {
        Object response = receiver.receive(new MessageJournal() {
            @Override
            public Object sendTo(Message message, MessageReceiver receiver) {
                return receiver.receive(this, message);
            }

            @Override
            public Object memoize(Supplier<Object> supplier) {
                return TransientMessageJournal.this.memoize(supplier);
            }
        }, message);
        messages.add(message);

        return response;
    }

    @Override
    public Object memoize(Supplier<Object> supplier) {
        Object result = supplier.get();
        memoizations.add(result);
        return result;
    }

    public void replay(MessageReceiver receiver) {
        ArrayDeque<Object> memoizations = new ArrayDeque<>(this.memoizations);
        for (Message x : messages) {
            receiver.receive(new MessageJournal() {
                @Override
                public Object sendTo(Message message, MessageReceiver receiver) {
                    return receiver.receive(this, message);
                }

                @Override
                public Object memoize(Supplier<Object> supplier) {
                    return memoizations.pop();
                }
            }, x);
        }
    }
}
