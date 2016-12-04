package jorch;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MigratingSerializer implements Serializer {
    public static class MigratingObjectOutputStream extends ObjectOutputStream {
        public MigratingObjectOutputStream(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) throws IOException {
            /*try {
                // Should auto migrate as well?
                obj.getClass().getMethod("replace");
                throw new IllegalArgumentException("Cannot serialize deprecated object " + obj);
            } catch (NoSuchMethodException e) {

            }

            return obj;*/

            return MigratingSerializer.replaceObject(obj);
        }
    }

    @Override
    public void serialize(Object object, OutputStream outputStream) throws IOException {
        try(ObjectOutputStream cachedResultObjectOutputStream = new MigratingObjectOutputStream(outputStream)) {
            cachedResultObjectOutputStream.writeObject(object);
        }
        catch (NotSerializableException e) {
            e.toString();
        }
    }

    public static class MigratingObjectInputStream extends ObjectInputStream {
        public MigratingObjectInputStream(InputStream in) throws IOException {
            super(in);
            enableResolveObject(true);
        }

        protected Object resolveObject(Object obj) throws IOException {
            return replaceObject(obj);
        }
    }

    @Override
    public Object deserialize(InputStream inputStream) throws ClassNotFoundException, IOException {
        try (ObjectInputStream objectInputStream = new MigratingObjectInputStream(inputStream)) {
            return objectInputStream.readObject();
        }
    }

    private static Object replaceObject(Object obj) {
        try {
            Method replaceMethod = obj.getClass().getMethod("replace");
            Object replacement = replaceMethod.invoke(obj);
            return replaceObject(replacement);
        } catch (NoSuchMethodException e) {

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return obj;
    }

}
