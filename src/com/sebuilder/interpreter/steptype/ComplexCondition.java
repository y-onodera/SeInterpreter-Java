package com.sebuilder.interpreter.steptype;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.WaitFor;
import java.util.Arrays;
import java.util.List;

public class ComplexCondition implements Getter {
    private final List<WaitFor> condtions = Lists.newArrayList();

    public ComplexCondition(WaitFor condtion, WaitFor... aCondtions) {
        this.condtions.add(condtion);
        if (aCondtions != null) {
            Arrays.stream(aCondtions).forEach(it -> condtions.add(it));
        }
    }

    /**
     * @param ctx Current test run.
     * @return The value this getter gets, eg the page title.
     */
    @Override
    public String get(TestRun ctx) {
        for (WaitFor condition : this.condtions) {
            if (!condition.run(ctx)) {
                return "false";
            }
        }
        return "true";
    }

    /**
     * @return The name of the parameter to compare the result of the get to, or null if the get
     * returns a boolean "true"/"false".
     */
    @Override
    public String cmpParamName() {
        return null;
    }
}
