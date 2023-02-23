package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.pointcut.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.IntStream;

public record PointcutLoader(ImportLoader importLoader) {

    public Optional<Pointcut> load(final File f, final File baseDir) {
        final File path = new File(baseDir, f.getPath());
        if (path.exists()) {
            return this.load(path);
        }
        return this.load(f);
    }

    public Optional<Pointcut> load(final File f) {
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            return this.load(new JSONObject(new JSONTokener(r)), f.getAbsoluteFile().getParentFile());
        } catch (final Throwable e) {
            throw new AssertionError("error load:" + f.getAbsolutePath(), e);
        }
    }

    public Optional<Pointcut> load(final JSONObject jsonObject, final File parentFile) {
        return this.load(jsonObject, "pointcut", parentFile);
    }

    public Optional<Pointcut> load(final JSONObject jsonObject, final String key, final File parentFile) {
        if (jsonObject.get(key) instanceof JSONObject object) {
            return this.parseFilter(object, parentFile);
        }
        return this.load(jsonObject.getJSONArray(key), parentFile);
    }

    public Optional<Pointcut> load(final JSONArray pointcuts, final File baseDir) {
        return IntStream.range(0, pointcuts.length())
                .mapToObj(i -> this.parseFilter(pointcuts.getJSONObject(i), baseDir))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Pointcut::or);
    }


    protected Optional<Pointcut> parseFilter(final JSONObject pointcutJSON, final File baseDir) {
        final JSONArray keysA = pointcutJSON.names();
        return IntStream.range(0, keysA.length())
                .mapToObj(j -> this.parseFilter(pointcutJSON, keysA.getString(j), baseDir))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Pointcut::and);
    }

    protected Optional<Pointcut> parseFilter(final JSONObject json, final String key, final File baseDir) {
        if (key.equals("import")) {
            return this.importScript(json, key, baseDir);
        } else if (key.equals("type")) {
            return this.getTypeFilter(json, key);
        } else if (key.equals("negated")) {
            return Optional.of(new NegatedFilter(json.getBoolean(key)));
        } else if (key.equals("skip")) {
            return Optional.of(new SkipFilter(json.getBoolean(key)));
        } else if (key.startsWith("locator")) {
            return this.getLocatorFilter(json, key);
        }
        return this.getStringFilter(json, key);
    }

    protected Optional<Pointcut> importScript(final JSONObject src, final String key, final File baseDir) {
        return this.importLoader.load(src, key, (value, where) ->
                Optional.of(new ImportFilter(value, where, (path) -> this.load(path, baseDir).orElseThrow())));
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
