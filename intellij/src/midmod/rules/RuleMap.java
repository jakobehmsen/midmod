package midmod.rules;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;
import midmod.rules.actions.Action;
import midmod.rules.actions.Actions;
import midmod.rules.patterns.Pattern;
import midmod.rules.patterns.Patterns;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class RuleMap {
    public static class Node {
        private Hashtable<Object, Node> byEquals = new Hashtable<>();
        private Hashtable<Class<?>, Node> byType = new Hashtable<>();
        private Node bySequence;
        private Action action;

        public Node match(Object value, Map<String, Object> captures) {
            Node n;

            n = byEquals.get(value);

            if(n != null)
                return n;

            n = byType.get(value.getClass());

            if(n != null)
                return n;

            if(value instanceof List) {
                Consumable consumable = new ListConsumable((List<Object>)value);
                n = bySequence.match(consumable, captures);

                return consumable.atEnd() ? n : null;
            }

            return null;
        }

        public Node match(Consumable consumable, Map<String, Object> captures) {
            Node n = match(consumable.peek(), captures);
            consumable.consume();
            return consumable.atEnd() ? n : n.match(consumable, captures);
        }

        public Node byEquals(Object obj) {
            return byEquals.computeIfAbsent(obj, o -> new Node());
        }

        public Node byType(Class<?> type) {
            return byType.computeIfAbsent(type, t -> new Node());
        }

        public Node bySequence() {
            if(bySequence == null)
                bySequence = new Node();
            return bySequence;
        }
    }

    private Node root = new Node();
    private LinkedHashMap<Pattern, Action> rules = new LinkedHashMap<>();

    public void define(Pattern pattern, Action action) {
        Node node = pattern.findNode(root);
        node.action = action;
        rules.put(pattern, action);
    }

    public <T, R> void defineBinary(String operator, Class<T> lhsType, Class<R> rhsType, BiFunction<T, R, Object> function) {
        rules.put(Patterns.binary(operator, lhsType, rhsType), Actions.binary(function));
    }

    public Action resolve(Object value, Map<String, Object> captures) {
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

    private boolean isMatch(Object value, Map<String, Object> captures, Pattern x) {
        return x.matchesSingle(value, captures);
    }
}
