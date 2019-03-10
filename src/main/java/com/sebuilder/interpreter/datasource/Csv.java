/*
 * Copyright 2014 Sauce Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebuilder.interpreter.datasource;

import com.opencsv.CSVReader;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.TestRuns;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sebuilder.interpreter.datasource.Utils.findFile;

/**
 * CSV-based data source.
 *
 * @author zarkonnen
 */
public class Csv implements DataSource {
    @Override
    public List<Map<String, String>> getData(Map<String, String> config, File relativeTo, Map<String, String> vars) {
        ArrayList<Map<String, String>> data = new ArrayList<>();
        File f = findFile(relativeTo, TestRuns.replaceVariable(config.get("path"), vars));
        String charsetName = Context.getInstance().getDataSourceEncording();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(f), charsetName));
            CSVReader csvR = new CSVReader(r);
            String[] keys = csvR.readNext();
            if (keys != null) {
                String[] line;
                int rowNumber = 1;
                while ((line = csvR.readNext()) != null) {
                    rowNumber++;
                    HashMap<String, String> row = new HashMap<String, String>();
                    if (line.length < keys.length) {
                        throw new IOException("Not enough cells in row " + rowNumber + ".");
                    }
                    row.put(DataSource.ROW_NUMBER, String.valueOf(rowNumber - 1));
                    for (int c = 0; c < keys.length; c++) {
                        row.put(keys[c], line[c]);
                    }
                    data.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get data.", e);
        } finally {
            try {
                r.close();
            } catch (Exception e) {
            }
        }
        return data;
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
