/*
 * Copyright 2012 Sauce Labs
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

import com.google.common.base.Strings;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;

public interface WebDriverFactory {
    /**
     * @param config A key/value mapping of configuration options specific to this factory.
     * @return A RemoteWebDriver of the type produced by this factory.
     */
    RemoteWebDriver make(Map<String, String> config) throws Exception;

    default String targetBrowser() {
        return this.getClass().getSimpleName();
    }

    String getDriverName();

    String getDriverPath();

    boolean isBinarySelectable();

    default String getBinaryPath() {
        if (this.isBinarySelectable()) {
            return Context.getDriverConfig().getOrDefault("binary", null);
        }
        return null;
    }

    void setDriverPath(String driverPath);

    default void setBinaryPath(String binaryPath) {
        if (this.isBinarySelectable()) {
            if (Strings.isNullOrEmpty(binaryPath)) {
                Context.getDriverConfig().remove("binary");
            } else {
                Context.getDriverConfig().put("binary", binaryPath);
            }
        }
    }
}
