package newlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AnnotatableResource {
    private ArrayList<AnnotationResource> annotations = new ArrayList<>();

    public void addAnnotation(String typeName, Map<String, Object> args) {
        annotations.add(new AnnotationResource(typeName, args));
    }

    public List<AnnotationResource> getAnnotations() {
        return annotations;
    }
}
