package newlayer;

import java.util.Map;
import java.util.stream.Collectors;

public class AnnotationResource {
    private String typeName;
    private Map<String, Object> args;

    public AnnotationResource(String typeName, Map<String, Object> args) {
        this.typeName = typeName;
        this.args = args;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setArgument(String name, Object value) {
        args.put(name, value);
    }

    @Override
    public String toString() {
        return "@" + typeName + "(" +
            args.entrySet().stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.joining(", ")) +
            ")";
    }
}
