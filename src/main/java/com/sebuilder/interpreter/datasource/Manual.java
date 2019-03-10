/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sebuilder.interpreter.datasource;

import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.TestRuns;

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
    public List<Map<String, String>> getData(Map<String, String> config, File relativeTo, Map<String, String> vars) {
        config.put(DataSource.ROW_NUMBER, String.valueOf(1));
        config.keySet()
                .stream()
                .forEach(key -> config.put(key, TestRuns.replaceVariable(config.get(key), vars)));
        return Collections.singletonList(config);
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
