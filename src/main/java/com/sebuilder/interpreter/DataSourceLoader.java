package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DataSourceLoader {

    private final DataSource dataSource;

    private final Map<String, String> dataSourceConfig;

    private final File relativePath;

    private final InputData shareInput;

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

    public List<InputData> loadData() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSourceLoader that = (DataSourceLoader) o;
        return Objects.equal(dataSource, that.dataSource) &&
                Objects.equal(dataSourceConfig, that.dataSourceConfig) &&
                Objects.equal(relativePath, that.relativePath) &&
                Objects.equal(shareInput, that.shareInput);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dataSource, dataSourceConfig, relativePath, shareInput);
    }

    @Override
    public String toString() {
        return "DataSourceLoader{" +
                "dataSource=" + dataSource +
                ", dataSourceConfig=" + dataSourceConfig +
                ", relativePath=" + relativePath +
                ", shareInput=" + shareInput +
                '}';
    }

}
