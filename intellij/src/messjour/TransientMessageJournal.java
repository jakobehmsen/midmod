package messjour;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class TransientMessageJournal implements MessageJournal {
    private static interface Event {
        void replay(MessageJournal journal, ObjectPool objectPool);
    }

    private static class MessageSend implements Event {
        private Function<ObjectPool, MessageReceiver> targetSupplier;
        private Function<ObjectPool, Message> messageSupplier;

        private MessageSend(Function<ObjectPool, MessageReceiver> targetSupplier, Function<ObjectPool, Message> messageSupplier) {
            this.targetSupplier = targetSupplier;
            this.messageSupplier = messageSupplier;
        }

        @Override
        public void replay(MessageJournal journal, ObjectPool objectPool) {
            MessageReceiver target = targetSupplier.apply(objectPool);
            Message message = messageSupplier.apply(objectPool);
            target.receive(journal, message);
        }
    }

    private static class NewInstance<T> implements Event {
        private Supplier<T> instanceSupplier;

        private NewInstance(Supplier<T> instanceSupplier) {
            this.instanceSupplier = instanceSupplier;
        }

        @Override
        public void replay(MessageJournal journal, ObjectPool objectPool) {
            journal.newInstance(instanceSupplier);
        }
    }

    private class TransientMessage implements Message {
        private ObjectPool objectPool;
        private String name;
        private Object[] arguments;
        private boolean[] isReference;

        private TransientMessage(ObjectPool objectPool, String name, Object[] arguments, boolean[] isReference) {
            this.objectPool = objectPool;
            this.name = name;
            this.arguments = arguments;
            this.isReference = isReference;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int arity() {
            return arguments.length;
        }

        @Override
        public Object getArgument(int ordinal) {
            if(isReference[ordinal]) {
                int id = (int) arguments[ordinal];
                return objectPool.get(id);
            }
            return arguments[ordinal];
        }
    }

    private ArrayList<Event> messages = new ArrayList<>();
    private ArrayList<Object> memoizations = new ArrayList<>();
    private ObjectPool objectPool = new ObjectPool();
    //private IdentityHashMap<Object, Integer> referenceIdMap = new IdentityHashMap<>();
    //private Hashtable<Integer, Object> referenceMap = new Hashtable<>();

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

            @Override
            public <T> T newInstance(Supplier<T> supplier) {
                return TransientMessageJournal.this.newInstance(supplier);
            }
        }, message);

        String name = message.getName();
        Object[] argumentsToPersist = new Object[message.arity()];
        boolean[] isReference = new boolean[message.arity()];
        for(int i = 0; i < message.arity(); i++) {
            Object argument = message.getArgument(i);
            if(argument instanceof Integer) {
                argumentsToPersist[i] = argument;
                isReference[i] = false;
            } else {
                int id = objectPool.getId(argument);
                argumentsToPersist[i] = id;
                isReference[i] = true;
            }
        }
        //messages.add(new TransientMessage(message.getName(), argumentsToPersist, isReference));
        int receiverId = objectPool.getId(receiver);
        messages.add(new MessageSend(
            op -> (MessageReceiver) op.get(receiverId),
            op -> new TransientMessage(op, name, argumentsToPersist, isReference)
        ));

        return response;
    }

    @Override
    public Object memoize(Supplier<Object> supplier) {
        Object result = supplier.get();
        memoizations.add(result);
        return result;
    }

    @Override
    public <T> T newInstance(Supplier<T> supplier) {
        T instance = supplier.get();
        objectPool.add(instance);
        messages.add(new NewInstance(supplier));
        /*int id = referenceIdMap.size();
        referenceIdMap.put(instance, id);
        referenceMap.put(id, instance);*/
        return instance;
    }

    public void replay(ObjectPool mjObjectPool) {
        ArrayDeque<Object> memoizations = new ArrayDeque<>(this.memoizations);
        //ObjectPool mjObjectPool = new ObjectPool();
        MessageJournal mj = new MessageJournal() {
            @Override
            public Object sendTo(Message message, MessageReceiver receiver) {
                return receiver.receive(this, message);
            }

            @Override
            public Object memoize(Supplier<Object> supplier) {
                return memoizations.pop();
            }

            @Override
            public <T> T newInstance(Supplier<T> supplier) {
                T instance = supplier.get();
                mjObjectPool.add(instance);
                return instance;
            }
        };
        for (Event x : messages) {
            x.replay(mj, mjObjectPool);
        }
    }
}
