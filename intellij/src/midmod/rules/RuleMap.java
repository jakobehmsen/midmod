package midmod.rules;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;
import midmod.pal.evaluation.Instruction;
import midmod.rules.actions.Action;
import midmod.rules.actions.Actions;
import midmod.rules.patterns.Pattern;
import midmod.rules.patterns.Patterns;

import java.util.*;
import java.util.function.BiFunction;

public class RuleMap {
    public static class Node {
        private ArrayList<Map.Entry<EdgePattern, Node>> edges = new ArrayList<>();
        private Object action;

        public Node match(Consumable consumable, Environment captures, RuleMap local) {
            return edges.stream().map(x -> x.getKey().matches(x.getValue(), consumable, captures, local)).filter(x -> x != null).findFirst().orElse(null);
        }

        public Node byPattern(EdgePattern pattern) {
            Map.Entry<EdgePattern, Node> e = edges.stream().filter(x ->
                x.getKey().equals(pattern)
            ).findFirst().orElse(null);

            if(e == null) {
                e = new AbstractMap.SimpleImmutableEntry<>(pattern, new Node());
                edges.add(e);
                edges.sort((o1, o2) -> o1.getKey().pattern().compareTo(o2.getKey().pattern()));
            }

            return e.getValue();
        }

        public Iterable<? extends Map.Entry<EdgePattern, Node>> edges() {
            return edges;
        }

        public EdgePattern getEdge(Node node) {
            return edges.stream().filter(x -> x.getValue() == node).map(x -> x.getKey()).findFirst().orElse(null);
        }
    }

    private Node root = new Node();
    private LinkedHashMap<Pattern, Object> rules = new LinkedHashMap<>();

    public boolean defines(Pattern pattern) {
        //return rules.containsKey(pattern);
        return rules.keySet().stream().anyMatch(x -> x.equals(pattern));
    }

    public void define(Pattern pattern, Action action) {
        define(pattern, (Object)action);
    }

    public void define(Pattern pattern, Object action) {
        Node node = pattern.findNode(root);
        if(node == null)
            node = pattern.findNode(root);
        node.action = action;
        rules.put(pattern, action);
    }

    public <T, R> void defineBinary(String operator, Class<T> lhsType, Class<R> rhsType, BiFunction<T, R, Object> function) {
        define(Patterns.binary(operator, lhsType, rhsType), Actions.binary(function));
    }

    public Action resolve(Object value, Environment captures, RuleMap local) {
        // Wrap into Consumable
        Consumable consumable = new ListConsumable(Arrays.asList(value));
        Node node = root.match(consumable, captures, local);

        if(node != null)
            return (Action)node.action;

        return null;
    }

    public List<Instruction> resolveInstructions(Object value, Environment captures, RuleMap local) {
        // Wrap into Consumable
        Consumable consumable = new ListConsumable(Arrays.asList(value));
        Node node = root.match(consumable, captures, local);

        if(node != null)
            return (List<Instruction>) node.action;

        return null;
    }
}
