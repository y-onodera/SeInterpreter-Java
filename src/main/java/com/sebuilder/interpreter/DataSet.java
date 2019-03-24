package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DataSet {

    private final DataSource dataSource;

    private final Map<String, String> dataSourceConfig;

    private final File relativePath;

    public DataSet(DataSource dataSource, Map<String, String> dataSourceConfig, File relativePath) {
        this.dataSource = dataSource;
        if (dataSourceConfig != null) {
            this.dataSourceConfig = Maps.newHashMap(dataSourceConfig);
        } else {
            this.dataSourceConfig = Maps.newHashMap();
        }
        this.relativePath = relativePath;
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

    public List<TestData> loadData() {
        return this.loadData(new TestData());
    }

    public List<TestData> loadData(TestData vars) {
        if (this.dataSource == null) {
            return Lists.newArrayList(new TestData());
        }
        return this.dataSource.getData(this.dataSourceConfig, this.relativePath, vars);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSet dataSet = (DataSet) o;
        return Objects.equal(getDataSource(), dataSet.getDataSource()) &&
                Objects.equal(getDataSourceConfig(), dataSet.getDataSourceConfig()) &&
                Objects.equal(getRelativePath(), dataSet.getRelativePath());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDataSource(), getDataSourceConfig(), getRelativePath());
    }

    @Override
    public String toString() {
        return "DataSet{" +
                "dataSource=" + dataSource +
                ", dataSourceConfig=" + dataSourceConfig +
                ", relativePath=" + relativePath +
                '}';
    }
}
