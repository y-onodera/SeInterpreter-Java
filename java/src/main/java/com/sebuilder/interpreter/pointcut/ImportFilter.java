package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record ImportFilter(String path, String where,
                           Function<File, Pointcut> loadFunction) implements Pointcut.ExportablePointcut {

    @Override
    public boolean isHandle(final TestRun testRun, final Step step, final InputData var) {
        return this.materialize(var).load().isHandle(testRun, step, var);
    }

    @Override
    public ImportFilter materialize(final InputData var) {
        final String runtimePath = var.evaluateString(this.path);
        final String runtimeBaseDir = var.evaluateString(this.where);
        return new ImportFilter(runtimePath, runtimeBaseDir, this.loadFunction);
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
        return ExportablePointcut.super.value();
    }

    public Pointcut load() {
        return this.loadFunction.apply(Optional.ofNullable(this.where)
                .filter(it -> !"".equals(it))
                .map(it -> new File(it, this.path))
                .orElse(new File(this.path)));
    }

}
