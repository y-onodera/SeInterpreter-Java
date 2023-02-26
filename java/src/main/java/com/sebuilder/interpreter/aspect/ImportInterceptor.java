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
                                Function<File, Iterable<Interceptor>> loadFunction,
                                boolean takeOverChain) implements Interceptor.ExportableInterceptor {

    public ImportInterceptor(final String path, final String where,
                             final Function<File, Iterable<Interceptor>> loadFunction) {
        this(path, where, loadFunction, true);
    }

    @Override
    public boolean isTakeOverChain() {
        return this.takeOverChain;
    }

    @Override
    public Interceptor takeOverChain(final boolean newValue) {
        return new ImportInterceptor(this.path, this.where, this.loadFunction, newValue);
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
                .flatMap(it -> it.takeOverChain(this.takeOverChain).materialize(shareInput));
    }

    @Override
    public Map<String, String> stringParams() {
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
        return ExportableInterceptor.super.value();
    }

}
