package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.Getter;
import com.sebuilder.interpreter.step.GetterUseStep;

import java.util.Objects;

public class ExecCmd implements GetterUseStep {

    private Cmd cmd = new Cmd();

    @Override
    public Getter getGetter() {
        return this.cmd;
    }

    @Override
    public boolean run(TestRun ctx) {
        if (ctx.containsKey(this.getGetter().cmpParamName())) {
            return this.test(ctx);
        }
        return Objects.equals("0", this.getGetter().get(ctx));
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("cmd")) {
            o.put("cmd", "");
        }
        return o;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
