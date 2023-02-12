package com.sebuilder.interpreter.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import org.json.JSONObject;

import java.io.File;
import java.util.function.BiFunction;

public class ImportLoader {

    public <R> R load(final JSONObject src, final File baseDir, final BiFunction<File, JSONObject, R> loadFunction) {
        final String path = Context.bindEnvironmentProperties(src.getString("path"));
        if (src.has("where") && !Strings.isNullOrEmpty(src.getString("where"))) {
            final File wherePath = new File(Context.bindEnvironmentProperties(src.getString("where")), path);
            return loadFunction.apply(wherePath, src);
        }
        File f = new File(path);
        if (!f.exists()) {
            f = new File(baseDir, path);
        }
        return loadFunction.apply(f.getAbsoluteFile(), src);
    }

}
