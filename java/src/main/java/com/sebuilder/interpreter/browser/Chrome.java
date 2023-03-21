package com.sebuilder.interpreter.browser;

import com.sebuilder.interpreter.WebDriverFactory;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Chrome implements WebDriverFactory {

    /**
     * @param config A key/value mapping of configuration options specific to this factory.
     * @return A RemoteWebDriver of the type produced by this factory.
     */
    @Override
    public RemoteWebDriver createLocaleDriver(final Map<String, String> config) {
        return new ChromeDriver(this.getOptions(config));
    }

    @Override
    public ChromeOptions getOptions(final Map<String, String> config) {
        final HashMap<String, String> caps = new HashMap<>();
        final HashMap<String, Object> prefs = new HashMap<>();
        final ChromeOptions option = new ChromeOptions();
        config.forEach((key, value) -> {
            if (key.equals("binary")) {
                option.setBinary(new File(value));
            } else if (key.startsWith("experimental.")) {
                switch (value.toLowerCase()) {
                    case "true", "false" -> prefs.put(key.substring("experimental.".length()), Boolean.valueOf(value));
                    default -> prefs.put(key.substring("experimental.".length()), value);
                }
            } else if (key.startsWith("chrome.arguments.")) {
                if (!Optional.ofNullable(config.get(key)).orElse("").isBlank()) {
                    option.addArguments("--" + key.substring("chrome.arguments.".length()) + "=" + config.get(key));
                } else {
                    option.addArguments("--" + key.substring("chrome.arguments.".length()));
                }
            } else {
                caps.put(key, value);
            }
        });
        option.setExperimentalOption("prefs", prefs);
        return option.merge(new DesiredCapabilities(caps));
    }

    @Override
    public void setDriverPath(final String driverPath) {
        System.setProperty("webdriver.chrome.driver", driverPath);
    }

    @Override
    public String getDriverPath() {
        return System.getProperty("webdriver.chrome.driver");
    }

    @Override
    public String getDriverName() {
        return "chromedriver";
    }

}
