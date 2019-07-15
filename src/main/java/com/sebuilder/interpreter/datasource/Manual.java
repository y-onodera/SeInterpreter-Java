/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sebuilder.interpreter.datasource;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.TestData;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manual-input data source.
 *
 * @author zarkonnen
 */
public class Manual implements DataSource {
    @Override
    public List<TestData> getData(Map<String, String> config, File relativeTo, TestData vars) {
        Map<String, String> row = new HashMap<>(config);
        row.put(TestData.ROW_NUMBER, String.valueOf(1));
        config.keySet()
                .stream()
                .forEach(key -> row.put(key, vars.bind(config.get(key))));
        return Lists.newArrayList(new TestData(row).lastRow(true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
