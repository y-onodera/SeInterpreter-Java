package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record ImportFilter(String path, String where,
                           Function<File, Pointcut> loadFunction) implements Pointcut.ExportablePointcut {

    @Override
    public boolean isHandle(final Step step, final InputData var) {
        return this.materialize(var).isHandle(step, var);
    }

    @Override
    public Pointcut materialize(final InputData var) {
        final String runtimePath = var.evaluateString(this.path);
        final String runtimeBaseDir = var.evaluateString(this.where);
        File f = new File(runtimeBaseDir, runtimePath);
        if (!f.exists()) {
            f = new File(runtimePath);
        }
        return this.loadFunction.apply(f);
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
        return ExportablePointcut.super.value();
    }

}
