package com.sebuilder.interpreter;

import org.json.JSONException;
import org.json.JSONObject;

public interface GetterUseStep extends StepType {

    Getter getGetter();

    @Override
    default String getStepTypeName() {
        final String typeName = StepType.super.getStepTypeName();
        return typeName.substring(0, 1).toLowerCase() + typeName.substring(1) + this.getGetter().getClass().getSimpleName();
    }

    default boolean test(TestRun ctx) {
        String got = this.getGetter().get(ctx);
        boolean result = this.getGetter().cmpParamName() == null
                ? Boolean.parseBoolean(got)
                : ctx.string(this.getGetter().cmpParamName()).equals(got);
        return result != ctx.currentStep().isNegated();
    }

    @Override
    default void supplementSerialized(JSONObject o) throws JSONException {
        this.getGetter().supplementSerialized(o);
        if (this.getGetter().cmpParamName() != null) {
            if (!o.has(this.getGetter().cmpParamName())) {
                o.put(this.getGetter().cmpParamName(), "");
            }
        }
    }
}
