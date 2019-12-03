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

import com.google.common.io.Files;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.DataSourceWriter;
import com.sebuilder.interpreter.TestData;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CSV-based data source.
 *
 * @author zarkonnen
 */
public class Csv implements FileDataSource {

    @Override
    public List<TestData> getData(Map<String, String> config, File relativeTo, TestData vars) {
        ArrayList<TestData> data = new ArrayList<>();
        File f = this.sourceFile(config, relativeTo, vars);
        String charsetName = Context.getDataSourceEncoding();
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
                    LinkedHashMap<String, String> row = new LinkedHashMap<String, String>();
                    if (line.length < keys.length) {
                        throw new IOException("Not enough cells in row " + rowNumber + ".");
                    }
                    row.put(TestData.ROW_NUMBER, String.valueOf(rowNumber - 1));
                    for (int c = 0; c < keys.length; c++) {
                        row.put(keys[c], line[c]);
                    }
                    data.add(new TestData(row));
                }
                if (rowNumber > 1) {
                    final int lastRowNumber = rowNumber - 2;
                    TestData lastRow = data.get(lastRowNumber).lastRow(true);
                    data.remove(lastRowNumber);
                    data.add(lastRow);
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
    public DataSourceWriter writer(Map<String, String> dataSourceConfig, File relativePath, TestData shareInput) {
        return data -> {
            File target = sourceFile(dataSourceConfig, relativePath, shareInput);
            try (BufferedWriter writer = Files.newWriter(target, Charset.forName(Context.getDataSourceEncoding()))) {
                CSVWriter csvwriter = new CSVWriter(writer);
                String[] header = data.get(0).entrySet().stream().map(Map.Entry::getKey).toArray(size -> new String[size]);
                List<String[]> rows = data.stream()
                        .map(it -> it.entrySet().stream().map(Map.Entry::getValue).toArray(size -> new String[size]))
                        .collect(Collectors.toCollection(ArrayList::new));
                rows.add(0, header);
                csvwriter.writeAll(rows);
            }
        };
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
