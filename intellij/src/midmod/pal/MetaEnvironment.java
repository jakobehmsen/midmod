package midmod.pal;

import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.actions.Action;
import midmod.rules.actions.Call;
import midmod.rules.actions.Constant;
import midmod.rules.actions.LoadAsConstant;
import midmod.rules.patterns.Pattern;
import midmod.rules.patterns.Patterns;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

public class MetaEnvironment {
    private MetaEnvironment outer;
    private Map<String, Integer> nameToCaptureAddressMap = new Hashtable<>();
    //private Set<String> nameToCaptureAddressMapClosed = new HashSet<>(); // The captures to bind as rules to the rule map
    private Map<String, Integer> nameToCaptureAddressMapClosed = new Hashtable<>();
    // requiresContext relates to MetaEnvironments that maps to RuleMaps
    private boolean requiresContext; // Should a special context rule be bind to the creation rule map?

    public MetaEnvironment(MetaEnvironment outer) {
        this.outer = outer;
    }

    private Action createActionForInner(MetaEnvironment target, MetaEnvironment current, String name, int distance) {
        Integer index = nameToCaptureAddressMap.get(name);
        if(index != null) {
            //target.nameToDistanceMap.put(name, distance);
            //current.requiresContext = true;
            //nameToCaptureAddressMapClosed.add(name);
            current.nameToCaptureAddressMapClosed.put(name, index);
            // This action could probably be built by composing multiple Call actions
            return new Action() {
                @Override
                public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
                    RuleMap context = ruleMap;
                    for(int i = distance; i > 0; i--) {
                        context = (RuleMap) Call.onLocal(context, local, "context");
                    }
                    Object value = Call.onLocal(context, local, name);
                    return value;
                }

                @Override
                public Object toValue() {
                    return Arrays.asList("access", index);
                }
            };
        } else {
            if(outer != null) {
                //requiresContext = true;
                return outer.createActionForInner(target, this, name, distance + 1);
            }

            throw new UnsupportedOperationException("Name " + name + " not declared.");
        }
    }

    /*public Set<String> getNameToCaptureAddressMapClosed() {
        return nameToCaptureAddressMapClosed;
    }*/

    public boolean requiresContext() {
        return requiresContext;
    }

    public Action createActionFor(String name) {
        Integer index = nameToCaptureAddressMap.get(name);
        if(index != null) {
            return new Action() {
                @Override
                public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
                    Object val = captures.get(index);
                    return val;
                }

                @Override
                public Object toValue() {
                    return Arrays.asList("access", index);
                }
            };
        }
        if(outer != null) {
            return outer.createActionForInner(this, this, name, 0);
        }

        throw new UnsupportedOperationException("Name " + name + " not declared.");
    }

    public int size() {
        return nameToCaptureAddressMap.size();
    }

    public void put(String name, int index) {
        nameToCaptureAddressMap.put(name, index);
    }

    public int getIndexFor(String name) {
        return nameToCaptureAddressMap.get(name);
    }

    public void setupRuleMap(RuleMap context, RuleMap newRuleMap, Environment captures) {
        //if(requiresContext)
        //    newRuleMap.define(Patterns.equalsObject("context"), new Constant(context));

        nameToCaptureAddressMapClosed.entrySet().forEach(x -> {
            String name = x.getKey();
            int index = x.getValue();
            newRuleMap.define(Patterns.equalsObject(name), new Constant(captures.get(index)));
        });

        /*nameToDistanceMap.entrySet().forEach(x -> {
            String name = x.getKey();
            int distance = x.getValue();
        });*/
    }

    public void addClosedCaptures(Map<Pattern, Action> patternActionMap) {
        nameToCaptureAddressMapClosed.entrySet().forEach(x -> {
            String name = x.getKey();
            int index = x.getValue();
            patternActionMap.put(Patterns.equalsObject(name), new LoadAsConstant(index));
            //newRuleMap.define(Patterns.equalsObject(name), new Constant(captures.get(index)));
        });
    }
}
