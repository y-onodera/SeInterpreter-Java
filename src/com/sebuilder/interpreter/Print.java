package com.sebuilder.interpreter;

public class Print implements StepType {
    public final Getter getter;

    public Print(Getter getter) {
        this.getter = getter;
    }

    @Override
    public boolean run(TestRun ctx) {
        String value = getter.get(ctx);
        ctx.log().info(value);
        return true;
    }
}
