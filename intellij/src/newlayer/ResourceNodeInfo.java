package newlayer;

import javax.swing.*;

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

    public void open(Overview overview) {
        overview.open(resource);
        ViewBinding<JComponent> resourceView = overview.getView(resource);
        resourceView.select(nodeInfo);
    }
}
