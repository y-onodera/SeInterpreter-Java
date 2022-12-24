/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sebuilder.interpreter.datasource;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.InputData;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manual-input data source.
 *
 * @author zarkonnen
 */
public class Manual implements DataSource {
    @Override
    public List<InputData> getData(final Map<String, String> config, final File relativeTo, final InputData vars) {
        final LinkedHashMap<String, String> row = new LinkedHashMap<>(config);
        row.put(InputData.ROW_NUMBER, String.valueOf(1));
        config.keySet()
                .forEach(key -> row.put(key, vars.evaluateString(config.get(key))));
        return Lists.newArrayList(new InputData(row).lastRow(true));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
