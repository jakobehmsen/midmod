package midmod;

public class SingleClassLoader extends ClassLoader {
    private String name;
    private byte[] classBytes;

    public SingleClassLoader(String name, byte[] classBytes) {
        this.name = name;
        this.classBytes = classBytes;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(this.name.equals(name))
            return defineClass(name, classBytes, 0, classBytes.length);

        return getParent().loadClass(name);
    }
}
