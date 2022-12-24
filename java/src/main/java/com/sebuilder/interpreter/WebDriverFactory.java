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
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

public interface WebDriverFactory {
    /**
     * @param config A key/value mapping of configuration options specific to this factory.
     * @return A RemoteWebDriver of the type produced by this factory.
     */
    default RemoteWebDriver make(final Map<String, String> config) throws Exception {
        if (config.containsKey(Context.REMOTE_URL_KEY)) {
            final RemoteWebDriver result = new RemoteWebDriver(new URL(config.get(Context.REMOTE_URL_KEY))
                    , this.getOptions(config.entrySet()
                    .stream()
                    .filter(it -> !it.getKey().equals(Context.REMOTE_URL_KEY))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
            result.setFileDetector(new LocalFileDetector());
            return result;
        }
        return this.createLocaleDriver(config);
    }

    RemoteWebDriver createLocaleDriver(Map<String, String> config);

    Capabilities getOptions(Map<String, String> config);

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

    default void setBinaryPath(final String binaryPath) {
        if (this.isBinarySelectable()) {
            if (Strings.isNullOrEmpty(binaryPath)) {
                Context.getDriverConfig().remove("binary");
            } else {
                Context.getDriverConfig().put("binary", binaryPath);
            }
        }
    }
}
