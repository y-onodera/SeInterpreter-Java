package com.sebuilder.interpreter.aspect;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Interceptor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public record ImportInterceptor(String path, String where,
                                Function<File, Iterable<Interceptor>> loadFunction) implements Interceptor.Exportable {

    public ImportInterceptor(final String path, final Function<File, Iterable<Interceptor>> loadFunction) {
        this(path, "", loadFunction);
    }

    @Override
    public Stream<Interceptor> materialize(final InputData shareInput) {
        final String runtimePath = shareInput.evaluateString(this.path);
        final String runtimeBaseDir = shareInput.evaluateString(this.where);
        File f = new File(runtimeBaseDir, runtimePath);
        if (!f.exists()) {
            f = new File(runtimePath);
        }
        return StreamSupport.stream(this.loadFunction.apply(f).spliterator(), false)
                .flatMap(it -> it.materialize(shareInput));
    }

    @Override
    public Map<String, String> params() {
        final Map<String, String> result = new HashMap<>();
        if (!this.where.isBlank()) {
            result.put("path", this.path);
            result.put("where", this.where);
        }
        return result;
    }

    @Override
    public String value() {
        if (this.where.isBlank()) {
            return this.path;
        }
        return Interceptor.Exportable.super.value();
    }

}
