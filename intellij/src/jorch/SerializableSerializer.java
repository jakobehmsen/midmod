package jorch;

import java.io.*;

public class SerializableSerializer implements Serializer {
    @Override
    public void serialize(Object object, OutputStream outputStream) throws IOException {
        try(ObjectOutputStream cachedResultObjectOutputStream = new ObjectOutputStream(outputStream)) {
            cachedResultObjectOutputStream.writeObject(object);
        }
        catch (NotSerializableException e) {
            e.toString();
        }
    }

    @Override
    public Object deserialize(InputStream inputStream) throws ClassNotFoundException, IOException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return objectInputStream.readObject();
        }
    }
}
