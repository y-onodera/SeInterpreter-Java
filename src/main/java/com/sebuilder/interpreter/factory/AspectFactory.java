package com.sebuilder.interpreter.factory;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.Aspect;
import com.sebuilder.interpreter.Interceptor;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.pointcut.LocatorFilter;
import com.sebuilder.interpreter.pointcut.NegatedFilter;
import com.sebuilder.interpreter.pointcut.StepTypeFilter;
import com.sebuilder.interpreter.pointcut.StringParamFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Map;
import java.util.function.Predicate;

public class AspectFactory {

    private StepTypeFactory stepTypeFactory;

    AspectFactory(StepTypeFactory stepTypeFactory) {
        this.stepTypeFactory = stepTypeFactory;
    }

    StepTypeFactory getStepTypeFactory() {
        return stepTypeFactory;
    }

    public Aspect getAspect(File f) throws IOException, JSONException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            return this.getAspect(new JSONObject(new JSONTokener(r)));
        }
    }

    Aspect getAspect(JSONObject o) throws JSONException {
        Aspect result = new Aspect();
        if (o.has("aspect")) {
            Aspect.Builder builder = result.builder();
            JSONArray aspects = o.getJSONArray("aspect");
            for (int i = 0; i < aspects.length(); i++) {
                Interceptor.Builder interceptorBuilder = builder.interceptor();
                JSONObject aspect = aspects.getJSONObject(i);
                if (aspect.has("pointcut")) {
                    interceptorBuilder.setPointcut(this.getPointcut(aspect.getJSONArray("pointcut")));
                }
                if (aspect.has("before")) {
                    interceptorBuilder.addBefore(this.getStepTypeFactory().parseStep(aspect.getJSONObject("before")));
                }
                if (aspect.has("after")) {
                    interceptorBuilder.addAfter(this.getStepTypeFactory().parseStep(aspect.getJSONObject("after")));
                }
                if (aspect.has("failure")) {
                    interceptorBuilder.addFailure(this.getStepTypeFactory().parseStep(aspect.getJSONObject("failure")));
                }
                interceptorBuilder.build();
            }
            result = builder.build();
        }
        return result;
    }

    private Predicate<Step> getPointcut(JSONArray pointcuts) throws JSONException {
        Predicate<Step> result = Aspect.NONE;
        for (int i = 0; i < pointcuts.length(); i++) {
            result = result.or(this.parseFilter(pointcuts.getJSONObject(i)));
        }
        return result;
    }


    private Predicate<Step> parseFilter(JSONObject pointcutJSON) throws JSONException {
        Predicate<Step> pointcut = Aspect.APPLY;
        JSONArray keysA = pointcutJSON.names();
        Map<String, String> stringParam = Maps.newHashMap();
        Map<String, Locator> locatorParam = Maps.newHashMap();
        for (int j = 0; j < keysA.length(); j++) {
            String key = keysA.getString(j);
            if (key.equals("type")) {
                pointcut = pointcut.and(new StepTypeFilter(pointcutJSON.getString(key)));
            } else if (key.equals("negated")) {
                pointcut = pointcut.and(new NegatedFilter(pointcutJSON.getBoolean(key)));
            } else if (key.startsWith("locator")) {
                JSONObject locatorJSON = pointcutJSON.getJSONObject(key);
                locatorParam.put(key, new Locator(locatorJSON.getString("type"), locatorJSON.getString("value")));
            } else {
                stringParam.put(key, pointcutJSON.getString(key));
            }
        }
        if (locatorParam.size() > 0) {
            pointcut = pointcut.and(new LocatorFilter(locatorParam));
        }
        if (stringParam.size() > 0) {
            pointcut = pointcut.and(new StringParamFilter(stringParam));
        }
        return pointcut;
    }
}
