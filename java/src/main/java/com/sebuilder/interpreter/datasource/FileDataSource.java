package com.sebuilder.interpreter.datasource;

import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.InputData;

import java.io.File;
import java.util.Map;

import static com.sebuilder.interpreter.Utils.findFile;

public interface FileDataSource extends DataSource {

    default File sourceFile(final Map<String, String> config, final File relativeTo, final InputData vars) {
        return findFile(relativeTo, this.name(config, vars));
    }

    @Override
    default String name(final Map<String, String> dataSourceConfig, final InputData shareInput) {
        return shareInput.evaluateString(dataSourceConfig.get("path"));
    }

    @Override
    default boolean isLoadable(final Map<String, String> dataSourceConfig, final File relativePath, final InputData shareInput) {
        return this.sourceFile(dataSourceConfig, relativePath, shareInput).exists();
    }

    @Override
    default boolean enableMultiLine() {
        return true;
    }
}
