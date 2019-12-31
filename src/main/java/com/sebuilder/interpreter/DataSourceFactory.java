package com.sebuilder.interpreter;

public interface DataSourceFactory {

    DataSource getDataSource(String sourceName);
}
