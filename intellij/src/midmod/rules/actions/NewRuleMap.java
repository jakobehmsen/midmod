package midmod.rules.actions;

import midmod.pal.MetaEnvironment;
import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.patterns.Pattern;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class NewRuleMap implements Action {
    //private MetaEnvironment nameToCaptureAddressMapForDef;
    private Map<Pattern, Action> patternActionMap;

    public NewRuleMap(/*MetaEnvironment nameToCaptureAddressMapForDef, */Map<Pattern, Action> patternActionMap) {
        //this.nameToCaptureAddressMapForDef = nameToCaptureAddressMapForDef;
        this.patternActionMap = patternActionMap;
    }

    @Override
    public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
        RuleMap newRuleMap = new RuleMap();

        //nameToCaptureAddressMapForDef.setupRuleMap(ruleMap, newRuleMap, captures);

        patternActionMap.entrySet().forEach(x -> {
            //newRuleMap.define(x.getKey(), x.getValue());
            Action action = (Action)x.getValue().perform(ruleMap, local, captures);
            newRuleMap.define(x.getKey(), action);
        });

        return newRuleMap;
    }

    @Override
    public Object toValue() {
        //return Arrays.asList("map", slots.stream().map(x -> Arrays.asList(x.getKey(), x.getValue().toValue())).collect(Collectors.toList()));
        return Arrays.asList("new-rule-map", patternActionMap.entrySet().stream().map(x -> Arrays.asList(x.getKey().toValue(), x.getValue().toValue())).collect(Collectors.toList()));
    }
}
