package com.sebuilder.interpreter.datasource;

import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.TestData;

import java.io.File;
import java.util.Map;

import static com.sebuilder.interpreter.Utils.findFile;

public interface FileDataSource extends DataSource {

    default File sourceFile(Map<String, String> config, File relativeTo, TestData vars) {
        return findFile(relativeTo, this.name(config, vars));
    }

    @Override
    default String name(Map<String, String> dataSourceConfig, TestData shareInput) {
        return shareInput.bind(dataSourceConfig.get("path"));
    }

    default boolean isLoadable(Map<String, String> dataSourceConfig, File relativePath, TestData shareInput) {
        return this.sourceFile(dataSourceConfig, relativePath, shareInput).exists();
    }
}
