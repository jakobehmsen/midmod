package newlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LayerMutator {
    private Layer layer;
    private Supplier<NodeInfo> nodeInfoSupplier;
    private ArrayList<ClassResourceMutator> classes;

    public LayerMutator(Supplier<NodeInfo> nodeInfoSupplier) {
        this.nodeInfoSupplier = nodeInfoSupplier;
    }

    private ResourceNodeInfo getResourceInfoNode() {
        return new ResourceNodeInfo(layer, nodeInfoSupplier.get());
    }

    public void setLayer(Layer layer) {
        if(this.layer != null) {
            List<ClassResource> innerLayerClasses = classes.stream().map(x -> x.getClassResource()).collect(Collectors.toList());
            classes = new ArrayList();

            innerLayerClasses.forEach(x -> {
                classes.add(new ClassResourceMutator(x.copy(layer), () -> getResourceInfoNode()));
            });
        } else {
            classes = new ArrayList<>();
        }

        this.layer = layer;
    }

    public void addClass(String name) {
        classes.add(new ClassResourceMutator(new ClassResource(new ResourceNodeInfo(layer, nodeInfoSupplier.get()), name), () -> getResourceInfoNode()));
    }

    public List<ClassResourceMutator> getClasses() {
        return classes.stream().collect(Collectors.toList());
    }

    public ClassResourceMutator getClass(String name) {
        return getClasses().stream().filter(x -> x.getName().equals(name)).findFirst().get();
    }

    public void setClasses() {
        layer.setClasses(classes.stream().map(x -> x.getClassResource()).collect(Collectors.toList()));
    }
}
