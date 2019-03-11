/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sebuilder.interpreter.datasource;

import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.TestData;

import java.io.File;
import java.util.Collections;
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
        config.put(TestData.ROW_NUMBER, String.valueOf(1));
        config.keySet()
                .stream()
                .forEach(key -> config.put(key, vars.bind(config.get(key))));
        return Collections.singletonList(new TestData(config));
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
