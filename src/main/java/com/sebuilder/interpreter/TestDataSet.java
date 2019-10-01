package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TestDataSet {

    private final DataSource dataSource;

    private final Map<String, String> dataSourceConfig;

    private final File relativePath;

    public TestDataSet(DataSource dataSource, Map<String, String> dataSourceConfig, File relativePath) {
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
        TestDataSet testDataSet = (TestDataSet) o;
        return Objects.equal(getDataSource(), testDataSet.getDataSource()) &&
                Objects.equal(getDataSourceConfig(), testDataSet.getDataSourceConfig()) &&
                Objects.equal(getRelativePath(), testDataSet.getRelativePath());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDataSource(), getDataSourceConfig(), getRelativePath());
    }

    @Override
    public String toString() {
        return "TestDataSet{" +
                "dataSource=" + dataSource +
                ", dataSourceConfig=" + dataSourceConfig +
                ", relativePath=" + relativePath +
                '}';
    }
}
