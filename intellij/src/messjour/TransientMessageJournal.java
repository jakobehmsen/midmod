package messjour;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class TransientMessageJournal implements MessageJournal {
    public static class SerializedEvent {
        private byte[] bytes;
        private List<Object> memoizations;

        public SerializedEvent(byte[] bytes, List<Object> memoizations) {
            this.bytes = bytes;
            this.memoizations = memoizations;
        }
    }

    public interface Event {
        void replay(MessageJournal journal, ObjectPool objectPool);
    }

    public static class MessageSend implements Event, Serializable {
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

    public static class IdTargetSupplier implements Function<ObjectPool, MessageReceiver>, Serializable {
        private int id;

        public IdTargetSupplier(int id) {
            this.id = id;
        }

        @Override
        public MessageReceiver apply(ObjectPool objectPool) {
            return (MessageReceiver) objectPool.get(id);
        }
    }

    public static class TransientMessageSupplier implements Function<ObjectPool, Message>, Serializable {
        private Message message;
        private String name;
        private Object[] argumentsToPersist;
        private boolean[] isReference;

        public TransientMessageSupplier(ObjectPool objectPool, Message message) {
            this.message = message;
            /*name = message.getName();
            argumentsToPersist = new Object[message.arity()];
            isReference = new boolean[message.arity()];

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
            }*/
        }

        @Override
        public Message apply(ObjectPool objectPool) {
            //return new TransientMessage(objectPool, name, argumentsToPersist, isReference);
            return message;
        }
    }

    public static class NewInstance<T> implements Event, Serializable {
        private Supplier<T> instanceSupplier;

        private NewInstance(Supplier<T> instanceSupplier) {
            this.instanceSupplier = instanceSupplier;
        }

        @Override
        public void replay(MessageJournal journal, ObjectPool objectPool) {
            journal.newInstance(instanceSupplier);
        }
    }

    public static class TransientMessage implements Message, Serializable {
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

    private ArrayList<SerializedEvent> messages = new ArrayList<>();
    private ArrayList<Object> memoizations = new ArrayList<>();
    private ObjectPool objectPool = new ObjectPool();

    @Override
    public Object sendTo(Message message, MessageReceiver receiver) {
        prepareEvent();

        Object response = receiver.receive(new MessageJournal() {
            @Override
            public Object sendTo(Message message, MessageReceiver receiver) {
                return receiver.receive(this, message);
            }

            @Override
            public <T> T memoize(Supplier<T> supplier) {
                return TransientMessageJournal.this.memoize(supplier);
            }

            @Override
            public <T> T newInstance(Supplier<T> supplier) {
                return TransientMessageJournal.this.newInstance(supplier);
            }

        }, message);

        int receiverId = objectPool.getId(receiver);
        //Event event = new MessageSend(new IdTargetSupplier(receiverId), new TransientMessageSupplier(objectPool, message));
        Event event = new MessageSend(new IdTargetSupplier(receiverId), new TransientMessageSupplier(objectPool, message));

        addEvent(event);

        return response;
    }

    private void prepareEvent() {
        memoizations = new ArrayList<>();
    }

    private void addEvent(Event event) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            write(event, outputStream, objectPool);
            byte[] bytes = outputStream.toByteArray();
            messages.add(new SerializedEvent(bytes, memoizations));
            memoizations = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ObjectPoolObject implements Serializable {
        private int id;
        private Object object;
        private ObjectPool objectPool;

        public ObjectPoolObject(Object object) {
            this.object = object;
        }

        private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
            //out.writeUTF(c.getName());
            id = ((TransientMessageJournal.ObjectPoolObjectOutputStream)out).getObjectPool().getId(object);
            out.writeInt(id);
        }

        private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
            objectPool = ((ObjectPoolObjectInputStream)in).getObjectPool();
            id = in.readInt();
            System.out.println("Reading ObjectPoolObject");
        }

        private Object readResolve() throws ObjectStreamException {
            System.out.println("Resolving ObjectPoolObject");
            return objectPool.get(id);
        }
    }

    public static class ObjectPoolObjectOutputStream extends ObjectOutputStream {
        private ObjectPool objectPool;

        public ObjectPoolObjectOutputStream(OutputStream out, ObjectPool objectPool) throws IOException {
            super(out);
            this.objectPool = objectPool;
        }

        protected ObjectPoolObjectOutputStream() throws IOException, SecurityException { }

        public ObjectPool getObjectPool() {
            return objectPool;
        }
    }

    public static class ObjectPoolObjectInputStream extends ObjectInputStream {
        private ObjectPool objectPool;

        public ObjectPoolObjectInputStream(InputStream in, ObjectPool objectPool) throws IOException {
            super(in);
            this.objectPool = objectPool;
        }

        protected ObjectPoolObjectInputStream() throws IOException, SecurityException { }

        public ObjectPool getObjectPool() {
            return objectPool;
        }
    }

    private void write(Object obj, OutputStream outputStream, ObjectPool objectPool) throws IOException {
        //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectPoolObjectOutputStream(outputStream, objectPool);
        objectOutputStream.writeObject(obj);
    }

    private Object read(InputStream inputStream, ObjectPool objectPool) throws IOException, ClassNotFoundException {
        ObjectPoolObjectInputStream objectInputStream = new ObjectPoolObjectInputStream(inputStream, objectPool);
        return objectInputStream.readObject();
    }

    @Override
    public <T> T memoize(Supplier<T> supplier) {
        T result = supplier.get();
        memoizations.add(result);
        //addEvent(new Memoization(result));
        return result;
    }

    @Override
    public <T> T newInstance(Supplier<T> supplier) {
        prepareEvent();
        T instance = supplier.get();
        objectPool.add(instance);
        //messages.add(new NewInstance(supplier));

        addEvent(new NewInstance(supplier));
        /*int id = referenceIdMap.size();
        referenceIdMap.put(instance, id);
        referenceMap.put(id, instance);*/
        return instance;
    }

    public void replay(ObjectPool mjObjectPool) {
        ArrayDeque<Object> memoizations = new ArrayDeque<>();
        //ObjectPool mjObjectPool = new ObjectPool();
        MessageJournal mj = new MessageJournal() {
            @Override
            public Object sendTo(Message message, MessageReceiver receiver) {
                return receiver.receive(this, message);
            }

            @Override
            public <T> T memoize(Supplier<T> supplier) {
                return (T)memoizations.pop();
            }

            @Override
            public <T> T newInstance(Supplier<T> supplier) {
                T instance = supplier.get();
                mjObjectPool.add(instance);
                return instance;
            }
        };
        for (SerializedEvent sx : messages) {
            memoizations.clear();
            memoizations.addAll(sx.memoizations);
            Event x;
            try {
                x = (Event) read(new ByteArrayInputStream(sx.bytes), mjObjectPool);
                x.replay(mj, mjObjectPool);

                if(memoizations.size() > 0)
                    throw new RuntimeException("Not all memoizations were consumed.");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
