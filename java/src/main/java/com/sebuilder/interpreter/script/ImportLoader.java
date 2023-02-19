package com.sebuilder.interpreter.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import org.json.JSONObject;

import java.io.File;
import java.util.function.BiFunction;

public class ImportLoader {

    public <R> R load(final JSONObject src, final File baseDir, final BiFunction<File, JSONObject, R> loadFunction) {
        if (src.has("where") && !Strings.isNullOrEmpty(src.getString("where"))) {
            final File wherePath = this.toFile(src.getString("where"), src.getString("path"));
            return loadFunction.apply(wherePath, src);
        }
        return loadFunction.apply(this.toFile(baseDir, src.getString("path")), src);
    }

    protected File toFile(final String baseDir, final String path) {
        return this.toFile(new File(Context.bindEnvironmentProperties(baseDir)), path);
    }

    protected File toFile(final File baseDir, final String path) {
        File f = new File(baseDir, path);
        if (!f.exists()) {
            f = new File(Context.bindEnvironmentProperties(path));
        }
        return f.getAbsoluteFile();
    }

}
