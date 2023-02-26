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

package com.sebuilder.interpreter.browser;

import com.sebuilder.interpreter.WebDriverFactory;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public class Firefox implements WebDriverFactory {

    /**
     * @param config Key/value pairs treated as required capabilities, with the exception of:
     *               <ul>
     *               <li>binary: path to Firefox binary to use</li>
     *               <li>profile: path to Firefox profile to use</li>
     *               </ul>
     * @return A FirefoxDriver.
     */
    @Override
    public RemoteWebDriver createLocaleDriver(final Map<String, String> config) {
        return new FirefoxDriver(this.getOptions(config));
    }

    @Override
    public FirefoxOptions getOptions(final Map<String, String> config) {
        final FirefoxOptions option = new FirefoxOptions();
        config.forEach((key, value) -> {
            if (key.equals("binary")) {
                option.setBinary(new FirefoxBinary(new File(value)));
            } else if (key.equals("profile")) {
                option.setProfile(new FirefoxProfile(new File(value)));
            } else if (key.startsWith("firefox.arguments.")) {
                if (!Optional.ofNullable(config.get(key)).orElse("").isBlank()) {
                    option.addArguments("--" + key.substring("firefox.arguments.".length()) + "=" + config.get(key));
                } else {
                    option.addArguments("--" + key.substring("firefox.arguments.".length()));
                }
            } else {
                option.addPreference(key, value);
            }
        });
        return option;
    }

    @Override
    public void setDriverPath(final String driverPath) {
        System.setProperty("webdriver.gecko.driver", driverPath);
    }

    @Override
    public String getDriverPath() {
        return System.getProperty("webdriver.gecko.driver");
    }

    @Override
    public String getDriverName() {
        return "geckodriver";
    }

}
