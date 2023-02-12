package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.pointcut.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;
import java.util.stream.IntStream;

public class PointcutLoader {

    public Optional<Pointcut> getPointcut(final JSONArray pointcuts) {
        return IntStream.range(0, pointcuts.length())
                .mapToObj(i -> this.parseFilter(pointcuts.getJSONObject(i)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Pointcut::or);
    }


    protected Optional<Pointcut> parseFilter(final JSONObject pointcutJSON) {
        final JSONArray keysA = pointcutJSON.names();
        return IntStream.range(0, keysA.length())
                .mapToObj(j -> this.parseFilter(pointcutJSON, keysA, j))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Pointcut::and);
    }

    protected Optional<Pointcut> parseFilter(final JSONObject pointcutJSON, final JSONArray keysA, final int j) {
        final String key = keysA.getString(j);
        if (key.equals("type")) {
            return this.getTypeFilter(pointcutJSON, key);
        } else if (key.equals("negated")) {
            return Optional.of(new NegatedFilter(pointcutJSON.getBoolean(key)));
        } else if (key.equals("skip")) {
            return Optional.of(new SkipFilter(pointcutJSON.getBoolean(key)));
        } else if (key.startsWith("locator")) {
            return this.getLocatorFilter(pointcutJSON, key);
        }
        return this.getStringFilter(pointcutJSON, key);
    }

    protected Optional<Pointcut> getStringFilter(final JSONObject pointcutJSON, final String key) {
        if (pointcutJSON.get(key) instanceof JSONArray) {
            final JSONArray type = pointcutJSON.getJSONArray(key);
            return IntStream.range(0, type.length())
                    .mapToObj(k -> (Pointcut) new StringParamFilter(key, type.getString(k)))
                    .reduce(Pointcut::or);
        } else if (pointcutJSON.get(key) instanceof JSONObject) {
            final JSONObject type = pointcutJSON.getJSONObject(key);
            return Optional.of(new StringParamFilter(key, type.getString("value"), type.getString(Pointcut.METHOD_KEY)));
        }
        return Optional.of(new StringParamFilter(key, pointcutJSON.getString(key)));
    }

    protected Optional<Pointcut> getLocatorFilter(final JSONObject pointcutJSON, final String key) {
        final JSONObject locatorJSON = pointcutJSON.getJSONObject(key);
        if (locatorJSON.get("value") instanceof JSONArray) {
            final JSONArray values = locatorJSON.getJSONArray("value");
            return IntStream.range(0, values.length())
                    .mapToObj(k -> {
                        final Locator locator = new Locator(locatorJSON.getString("type"), values.getString(k));
                        return (Pointcut) new LocatorFilter(key, locator);
                    })
                    .reduce(Pointcut::or);
        }
        final Locator locator = new Locator(locatorJSON.getString("type"), locatorJSON.getString("value"));
        if (locatorJSON.has("method")) {
            return Optional.of(new LocatorFilter(key, locator, locatorJSON.getString("method")));
        }
        return Optional.of(new LocatorFilter(key, locator));
    }

    protected Optional<Pointcut> getTypeFilter(final JSONObject pointcutJSON, final String key) {
        if (pointcutJSON.get(key) instanceof JSONArray) {
            final JSONArray type = pointcutJSON.getJSONArray(key);
            return IntStream.range(0, type.length())
                    .mapToObj(k -> (Pointcut) new TypeFilter(type.getString(k)))
                    .reduce(Pointcut::or);
        } else if (pointcutJSON.get(key) instanceof JSONObject) {
            final JSONObject type = pointcutJSON.getJSONObject(key);
            return Optional.of(new TypeFilter(type.getString("value"), type.getString("method")));
        }
        return Optional.of(new TypeFilter(pointcutJSON.getString(key)));
    }
}
