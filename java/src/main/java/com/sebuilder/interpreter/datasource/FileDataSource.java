package com.sebuilder.interpreter.datasource;

import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.InputData;

import java.io.File;
import java.util.Map;

import static com.sebuilder.interpreter.Utils.findFile;

public interface FileDataSource extends DataSource {

    default File sourceFile(Map<String, String> config, File relativeTo, InputData vars) {
        return findFile(relativeTo, this.name(config, vars));
    }

    @Override
    default String name(Map<String, String> dataSourceConfig, InputData shareInput) {
        return shareInput.evaluateString(dataSourceConfig.get("path"));
    }

    default boolean isLoadable(Map<String, String> dataSourceConfig, File relativePath, InputData shareInput) {
        return this.sourceFile(dataSourceConfig, relativePath, shareInput).exists();
    }

    default boolean enableMultiLine() {
        return true;
    }
}
