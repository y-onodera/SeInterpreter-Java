package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.Aspect;
import com.sebuilder.interpreter.ExtraStepExecutor;
import com.sebuilder.interpreter.Interceptor;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.aspect.ImportInterceptor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

public record AspectLoader(Sebuilder sebuilder, PointcutLoader pointcutLoader) {

    public Aspect load(final File f, final File baseDir) {
        final File path = new File(baseDir, f.getPath());
        if (path.exists()) {
            return this.load(path);
        }
        return this.load(f);
    }

    public Aspect load(final File f) {
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            return this.load(new JSONObject(new JSONTokener(r)), f.getAbsoluteFile().getParentFile());
        } catch (final Throwable e) {
            throw new AssertionError("error load:" + f.getAbsolutePath(), e);
        }
    }

    public Aspect load(final String jsonText, final File f) {
        return this.load(new JSONObject(new JSONTokener(jsonText)), f.getAbsoluteFile());
    }

    public Aspect load(final JSONObject o, final File baseDir) {
        Aspect result = new Aspect();
        if (o.has("aspect")) {
            if (o.get("aspect") instanceof String) {
                final Interceptor imported = this.pointcutLoader.importLoader().load(o, "aspect"
                        , (value, where) -> new ImportInterceptor(value, where, (path) -> this.load(path, baseDir)));
                result = new Aspect(List.of(imported));
            } else if (o.get("aspect") instanceof JSONObject aspectObject) {
                result = new Aspect(List.of(this.toInterceptor(aspectObject, baseDir)));
            } else {
                final Aspect.Builder builder = result.builder();
                final JSONArray aspects = o.getJSONArray("aspect");
                IntStream.range(0, aspects.length()).forEach(i ->
                        builder.add(() -> {
                            final JSONObject aspect = aspects.getJSONObject(i);
                            return this.toInterceptor(aspect, baseDir);
                        })
                );
                result = builder.build();
            }
        }
        return result;
    }

    private Interceptor toInterceptor(final JSONObject aspect, final File baseDir) {
        if (aspect.has("import")) {
            return this.pointcutLoader.importLoader().load(aspect, "import", (value, where) ->
                    new ImportInterceptor(value, where, (path) -> this.load(path, baseDir)));
        }
        final ExtraStepExecutor.Builder interceptorBuilder = new ExtraStepExecutor.Builder();
        if (aspect.has("displayName")) {
            interceptorBuilder.setDisplayName(aspect.getString("displayName"));
        }
        if (aspect.has("pointcut")) {
            interceptorBuilder.setPointcut(this.pointcutLoader.load(aspect, baseDir)
                    .orElse(Pointcut.NONE));
        }
        if (aspect.has("before")) {
            interceptorBuilder.addBefore(this.sebuilder.load(aspect.getJSONObject("before"), null));
        }
        if (aspect.has("after")) {
            interceptorBuilder.addAfter(this.sebuilder.load(aspect.getJSONObject("after"), null));
        }
        if (aspect.has("failure")) {
            interceptorBuilder.addFailure(this.sebuilder.load(aspect.getJSONObject("failure"), null));
        }
        return interceptorBuilder.build();
    }


}
