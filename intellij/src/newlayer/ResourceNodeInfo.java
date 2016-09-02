package newlayer;

public class ResourceNodeInfo {
    private Resource resource;
    private NodeInfo nodeInfo;

    public ResourceNodeInfo(Resource resource, NodeInfo nodeInfo) {
        this.resource = resource;
        this.nodeInfo = nodeInfo;
    }

    public Resource getResource() {
        return resource;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }
}
