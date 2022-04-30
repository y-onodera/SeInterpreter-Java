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

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.InputData;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class None implements DataSource {
    @Override
    public List<InputData> getData(Map<String, String> config, File relativeTo, InputData vars) {
        ArrayList<InputData> l = Lists.newArrayList();
        final LinkedHashMap<String, String> row = new LinkedHashMap<>();
        row.put(InputData.ROW_NUMBER, String.valueOf(1));
        l.add(new InputData(row).lastRow(true));
        return l;
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
