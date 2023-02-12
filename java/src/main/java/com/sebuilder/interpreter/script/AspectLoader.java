package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.Aspect;
import com.sebuilder.interpreter.ExtraStepExecutor;
import com.sebuilder.interpreter.Pointcut;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.stream.IntStream;

public class AspectLoader {

    private final Sebuilder sebuilder;

    private final PointcutLoader pointcutLoader;

    public AspectLoader(final Sebuilder sebuilder, final PointcutLoader pointcutLoader) {
        this.sebuilder = sebuilder;
        this.pointcutLoader = pointcutLoader;
    }

    protected Aspect load(final JSONObject o) {
        Aspect result = new Aspect();
        if (o.has("aspect")) {
            final Aspect.Builder builder = result.builder();
            final JSONArray aspects = o.getJSONArray("aspect");
            IntStream.range(0, aspects.length()).forEach(i ->
                    builder.add(() -> {
                        final ExtraStepExecutor.Builder interceptorBuilder = new ExtraStepExecutor.Builder();
                        final JSONObject aspect = aspects.getJSONObject(i);
                        if (aspect.has("pointcut")) {
                            interceptorBuilder.setPointcut(this.pointcutLoader.getPointcut(aspect.getJSONArray("pointcut")).orElse(Pointcut.NONE));
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
                        return interceptorBuilder.get();
                    })
            );
            result = builder.build();
        }
        return result;
    }


}
