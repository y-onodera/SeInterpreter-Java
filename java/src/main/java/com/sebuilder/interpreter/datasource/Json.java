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
import com.sebuilder.interpreter.InputData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * JSON-based data source.
 *
 * @author zarkonnen
 */
public class Json implements FileDataSource {

    @Override
    public List<InputData> getData(final Map<String, String> config, final File relativeTo, final InputData vars) throws IOException {
        final ArrayList<InputData> data = new ArrayList<>();
        final File f = this.sourceFile(config, relativeTo, vars);
        final String charsetName = Context.getDataSourceEncoding();
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), charsetName))) {
            final JSONTokener tok = new JSONTokener(r);
            final JSONArray a = new JSONArray(tok);
            IntStream.range(0, a.length()).forEach(i -> {
                final JSONObject rowO = a.getJSONObject(i);
                final LinkedHashMap<String, String> row = new LinkedHashMap<>();
                row.put(InputData.ROW_NUMBER, String.valueOf(i + 1));
                rowO.keySet().forEach(key -> row.put(key, rowO.getString(key)));
                data.add(new InputData(row));
            });
            if (a.length() > 0) {
                final int lastRowNumber = a.length() - 1;
                final InputData lastRow = data.get(lastRowNumber).lastRow(true);
                data.remove(lastRowNumber);
                data.add(lastRow);
            }
        } catch (final JSONException e) {
            throw new IOException("Unable to get data.", e);
        }
        return data;
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
