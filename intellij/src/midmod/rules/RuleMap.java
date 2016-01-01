package midmod.rules;

import midmod.rules.actions.Action;
import midmod.rules.actions.Actions;
import midmod.rules.patterns.Pattern;
import midmod.rules.patterns.Patterns;

import java.util.*;
import java.util.function.BiFunction;

public class RuleMap {
    public static class Node {
        private ArrayList<Map.Entry<EdgePattern, Node>> edges = new ArrayList<>();

        //private SortedMap<EdgePattern, Node> sortedEdges = new TreeMap<>((o1, o2) -> o1.cardinality() - o2.cardinality());
        private SortedMap<EdgePattern, Node> sortedEdges = new TreeMap<>();

        /*private Hashtable<Object, Node> byEquals = new Hashtable<>();
        private Hashtable<Class<?>, Node> byType = new Hashtable<>();*/
        private Node bySequence;
        private Action action;

        public Node match(Object value, Environment captures) {
            /*Node n;

            n = byEquals.get(value);

            if(n != null)
                return n;

            n = byType.get(value.getClass());

            if(n != null)
                return n;*/

            /*if(value instanceof List) {
                Consumable consumable = new ListConsumable((List<Object>)value);
                Node n = bySequence.match(consumable, captures);

                return consumable.atEnd() ? n : null;
            }*/

            //return sortedEdges.entrySet().stream().filter(x -> x.getKey().matchesSingle(value, captures)).findFirst().get().getValue();

            return edges.stream().map(x -> x.getKey().matches(x.getValue(), value, captures)).filter(x -> x != null).findFirst().orElse(null);

            //return sortedEdges.entrySet().stream().filter(x -> x.getKey().matches(x.getValue(), value, captures) != null).findFirst().get().getValue();
        }

        /*public Node match(Consumable consumable, Map<String, Object> captures) {
            Node n = match(consumable.peek(), captures);
            consumable.consume();
            return consumable.atEnd() ? n : n.match(consumable, captures);
        }*/

        /*public Node byEquals(Object obj) {
            return byEquals.computeIfAbsent(obj, o -> new Node());
        }

        public Node byType(Class<?> type) {
            return byType.computeIfAbsent(type, t -> new Node());
        }*/

        public Node byPattern(EdgePattern pattern) {
            Map.Entry<EdgePattern, Node> e = edges.stream().filter(x -> x.getKey().equals(pattern)).findFirst().orElse(null);

            if(e == null) {
                e = new AbstractMap.SimpleImmutableEntry<>(pattern, new Node());
                edges.add(e);
                //edges.sort((o1, o2) -> o1.getKey().cardinality() - o2.getKey().cardinality());
                edges.sort((o1, o2) -> o1.getKey().pattern().compareTo(o2.getKey().pattern()));
            }

            return e.getValue();

            //return sortedEdges.computeIfAbsent(pattern, p -> new Node());
        }

        public Node bySequence() {
            if(bySequence == null)
                bySequence = new Node();
            return bySequence;
        }

        public EdgePattern getEdge(Node target) {
            return edges.stream().filter(x -> x.getValue() == target).findFirst().get().getKey();

            //return sortedEdges.entrySet().stream().filter(x -> x.getValue() == target).findFirst().get().getKey();
        }

        public void putPattern(EdgePattern edgePattern, Node target) {
            Map.Entry<EdgePattern, Node> e = edges.stream().filter(x -> x.getKey().equals(edgePattern)).findFirst().orElse(null);

            if(e == null) {
                e = new AbstractMap.SimpleImmutableEntry<>(edgePattern, target);
                edges.add(e);
                //edges.sort((o1, o2) -> o1.getKey().cardinality() - o2.getKey().cardinality());
                edges.sort((o1, o2) -> o1.getKey().pattern().compareTo(o2.getKey().pattern()));
            }

            //sortedEdges.put(edgePattern, target);
        }

        public Iterable<? extends Map.Entry<EdgePattern, Node>> edges() {
            return edges;
        }
    }

    private Node root = new Node();
    private LinkedHashMap<Pattern, Action> rules = new LinkedHashMap<>();

    public void define(Pattern pattern, Action action) {
        Node node = pattern.findNode(root);
        if(node == null)
            node = pattern.findNode(root);
        node.action = action;
        rules.put(pattern, action);
    }

    public <T, R> void defineBinary(String operator, Class<T> lhsType, Class<R> rhsType, BiFunction<T, R, Object> function) {
        //rules.put(Patterns.binary(operator, lhsType, rhsType), Actions.binary(function));
        define(Patterns.binary(operator, lhsType, rhsType), Actions.binary(function));
    }

    public Action resolve(Object value, Environment captures) {
        Node node = root.match(value, captures);

        if(node != null) {
            return node.action;
        }

        return null;

        //System.out.println(value);

        /*if(!rules.entrySet().stream().anyMatch(x -> isMatch(value, captures, x.getKey())))
            new String();

        return rules.entrySet().stream().filter(x -> isMatch(value, captures, x.getKey())).findFirst().get().getValue();*/
    }

    /*private boolean isMatch(Object value, Map<String, Object> captures, Pattern x) {
        return x.matchesSingle(value, captures);
    }*/
}
