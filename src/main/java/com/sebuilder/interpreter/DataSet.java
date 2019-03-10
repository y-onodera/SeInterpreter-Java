package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
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

    public List<Map<String, String>> loadData() {
        return this.loadData(Maps.newHashMap());
    }

    public List<Map<String, String>> loadData(Map<String, String> vars) {
        if (this.dataSource == null) {
            return Lists.newArrayList(new HashMap<>());
        }
        return this.dataSource.getData(this.dataSourceConfig, this.relativePath, vars);
    }

    public JSONObject toJSON() throws JSONException {
        if (this.dataSource != null) {
            JSONObject data = new JSONObject();
            final String sourceName = this.dataSource.getClass().getSimpleName().toLowerCase();
            data.put("source", sourceName);
            JSONObject configs = new JSONObject();
            configs.put(sourceName, this.dataSourceConfig);
            data.put("configs", configs);
            return data;
        }
        return null;
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
