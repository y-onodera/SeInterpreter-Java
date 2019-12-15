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

package com.sebuilder.interpreter;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A method of acquiring data rows for data-driven playback. Implementing classes should be located
 * in com.sebuilder.interpreter.datasource .
 *
 * @author zarkonnen
 */
public interface DataSource {

    DataSource NONE = new DataSource() {
        @Override
        public List<InputData> getData(Map<String, String> config, File relativeTo, InputData vars) {
            return Lists.newArrayList(new InputData());
        }

        @Override
        public String name() {
            return "none";
        }
    };

    List<InputData> getData(Map<String, String> config, File relativeTo, InputData vars);

    default String name(Map<String, String> dataSourceConfig, InputData shareInput) {
        return this.name();
    }

    default String name() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    default boolean isLoadable(Map<String, String> dataSourceConfig, File relativePath, InputData shareInput) {
        return false;
    }

    default DataSourceWriter writer(Map<String, String> dataSourceConfig, File relativePath, InputData shareInput) {
        throw new UnsupportedOperationException();
    }
}
