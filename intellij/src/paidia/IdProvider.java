package paidia;

public interface IdProvider {
    IdProvider forNewFrame();

    String nextId();
}
