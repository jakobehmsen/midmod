package messjour;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Supplier;

public class ClassBasedSupplier<T> implements Supplier<T>, Serializable {
    private Class<T> c;

    public ClassBasedSupplier(Class<T> c) {
        this.c = c;
    }

    @Override
    public T get() {
        try {
            return c.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.writeUTF(c.getName());
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        String name = in.readUTF();
        c = (Class<T>) Class.forName(name);
    }
}
