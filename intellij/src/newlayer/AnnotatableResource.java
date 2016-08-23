package newlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AnnotatableResource {
    private ArrayList<AnnotationResource> annotations = new ArrayList<>();

    public void addAnnotation(String typeName, Map<String, Object> args) {
        annotations.add(new AnnotationResource(typeName, args));
    }

    public List<AnnotationResource> getAnnotations() {
        return annotations;
    }

    public void copyTo(AnnotatableResource annotatableResource) {
        annotatableResource.annotations.addAll(annotations);
    }

    public boolean hasAnnotation(String typeName) {
        return annotations.stream().anyMatch(x -> x.getTypeName().equals(typeName));
    }
}
