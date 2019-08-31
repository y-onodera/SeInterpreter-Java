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

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.TestData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import static com.sebuilder.interpreter.Utils.findFile;

/**
 * JSON-based data source.
 *
 * @author zarkonnen
 */
public class Json implements DataSource {
    @Override
    public List<TestData> getData(Map<String, String> config, File relativeTo, TestData vars) {
        ArrayList<TestData> data = new ArrayList<>();
        File f = findFile(relativeTo, vars.bind(config.get("path")));
        String charsetName = Context.getInstance().getDataSourceEncoding();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), charsetName))) {
            JSONTokener tok = new JSONTokener(r);
            JSONArray a = new JSONArray(tok);
            for (int i = 0; i < a.length(); i++) {
                JSONObject rowO = a.getJSONObject(i);
                Map<String, String> row = new HashMap<>();
                row.put(TestData.ROW_NUMBER, String.valueOf(i + 1));
                for (Iterator<String> it = rowO.keys(); it.hasNext(); ) {
                    String key = it.next();
                    row.put(key, rowO.getString(key));
                }
                data.add(new TestData(row));
            }
            if (a.length() > 0) {
                final int lastRowNumber = a.length() - 1;
                TestData lastRow = data.get(lastRowNumber).lastRow(true);
                data.remove(lastRowNumber);
                data.add(lastRow);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get data.", e);
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
