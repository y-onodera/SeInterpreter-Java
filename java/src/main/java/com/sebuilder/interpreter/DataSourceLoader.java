package com.sebuilder.interpreter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record DataSourceLoader(DataSource dataSource, Map<String, String> dataSourceConfig, File relativePath,
                               InputData shareInput) {

    public DataSourceLoader(final DataSource dataSource, final Map<String, String> dataSourceConfig, final File relativePath) {
        this(dataSource, dataSourceConfig, relativePath, new InputData());
    }

    public DataSourceLoader(final DataSource dataSource, final Map<String, String> dataSourceConfig, final File relativePath, final InputData shareInput) {
        this.shareInput = shareInput;
        this.dataSource = dataSource;
        if (dataSourceConfig != null) {
            this.dataSourceConfig = new HashMap<>(dataSourceConfig);
        } else {
            this.dataSourceConfig = new HashMap<>();
        }
        this.relativePath = relativePath;
    }

    public DataSourceLoader shareInput(final InputData aShareInput) {
        return new DataSourceLoader(this.dataSource, this.dataSourceConfig, this.relativePath, aShareInput);
    }

    public List<InputData> loadData() throws IOException {
        return this.dataSource.getData(this.dataSourceConfig, this.relativePath, this.shareInput);
    }

    public String name() {
        return this.dataSource.name(this.dataSourceConfig, this.shareInput);
    }

    public boolean isLoadable() {
        return this.dataSource.isLoadable(this.dataSourceConfig, this.relativePath, this.shareInput);
    }

    public DataSourceWriter writer() {
        return this.dataSource.writer(this.dataSourceConfig, this.relativePath, this.shareInput);
    }

}
