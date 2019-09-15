package com.sebuilder.interpreter;

public interface DataSourceFactory {

    String getCustomDataSourcePackage();

    void setCustomDataSourcePackage(String customDataSourcePackage);

    DataSource getDataSource(String sourceName);
}
