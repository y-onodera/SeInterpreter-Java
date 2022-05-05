package com.sebuilder.interpreter;

import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public record DataSourceLoader(DataSource dataSource, Map<String, String> dataSourceConfig, File relativePath, InputData shareInput) {

    public DataSourceLoader(DataSource dataSource, Map<String, String> dataSourceConfig, File relativePath) {
        this(dataSource, dataSourceConfig, relativePath, new InputData());
    }

    public DataSourceLoader(DataSource dataSource, Map<String, String> dataSourceConfig, File relativePath, InputData shareInput) {
        this.shareInput = shareInput;
        this.dataSource = dataSource;
        if (dataSourceConfig != null) {
            this.dataSourceConfig = Maps.newHashMap(dataSourceConfig);
        } else {
            this.dataSourceConfig = Maps.newHashMap();
        }
        this.relativePath = relativePath;
    }

    public DataSourceLoader shareInput(InputData aShareInput) {
        return new DataSourceLoader(this.dataSource, this.dataSourceConfig, this.relativePath, aShareInput);
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public Map<String, String> getDataSourceConfig() {
        return Maps.newHashMap(this.dataSourceConfig);
    }

    public File getRelativePath() {
        return this.relativePath;
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
