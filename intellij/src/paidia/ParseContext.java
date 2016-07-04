package paidia;

public interface ParseContext {
    ParameterValue newParameter();

    IdProvider newIdProviderForFrame();
}
