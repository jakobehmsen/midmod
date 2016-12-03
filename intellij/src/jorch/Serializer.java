package jorch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {
    void serialize(Object object, OutputStream outputStream) throws IOException;
    Object deserialize(InputStream inputStream) throws ClassNotFoundException, IOException;
}
