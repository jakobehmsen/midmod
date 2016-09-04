package newlayer;

import java.util.List;
import java.util.function.Supplier;

public class ClassResourceMutator {
    private ClassResource classResource;
    private Supplier<ResourceNodeInfo> resourceNodeInfoSupplier;

    public ClassResourceMutator(ClassResource classResource, Supplier<ResourceNodeInfo> resourceNodeInfoSupplier) {
        this.classResource = classResource;
        this.resourceNodeInfoSupplier = resourceNodeInfoSupplier;
    }

    public void addField(String name, String accessModifier, String type) {
        ResourceNodeInfo resourceNodeInfo = resourceNodeInfoSupplier.get();
        classResource.addField(new ClassResource.FieldInfo(resourceNodeInfo, name, accessModifier, type));
    }

    public void addMethod(String name, String accessModifier, String returnType, List<ClassResource.ParameterInfo> parameters, String body) {
        ResourceNodeInfo resourceNodeInfo = resourceNodeInfoSupplier.get();
        classResource.addMethod(name, accessModifier, returnType, parameters, body);
    }

    public void addConstructor(String accessModifier, List<ClassResource.ParameterInfo> parameters, String body) {
        ResourceNodeInfo resourceNodeInfo = resourceNodeInfoSupplier.get();
        classResource.addConstructor(accessModifier, parameters, body);
    }

    public ClassResource.FieldInfo getField(String name) {
        return classResource.getField(name);
    }

    public List<ClassResource.FieldInfo> getFields() {
        return classResource.getFields();
    }

    public List<ClassResource.ConstructorInfo> getConstructors() {
        return classResource.getConstructors();
    }

    public void addInterfaceName(String interfaceName) {
        classResource.addInterfaceName(interfaceName);
    }

    public void setSuperClassName(String superClassName) {
        classResource.setSuperClassName(superClassName);
    }

    public String getName() {
        return classResource.getName();
    }

    public ClassResource getClassResource() {
        return classResource;
    }
}
