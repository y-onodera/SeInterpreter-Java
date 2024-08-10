package com.sebuilder.interpreter.browser;

import com.sebuilder.interpreter.WebDriverFactory;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Edge implements WebDriverFactory {

    @Override
    public RemoteWebDriver createLocaleDriver(final Map<String, String> config) {
        return new EdgeDriver(this.getOptions(config));
    }

    @Override
    public EdgeOptions getOptions(final Map<String, String> config) {
        final HashMap<String, String> caps = new HashMap<>();
        final HashMap<String, Object> prefs = new HashMap<>();
        final EdgeOptions option = new EdgeOptions();
        config.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().startsWith("firefox") && !entry.getKey().startsWith("chrome"))
                .forEach(entry -> {
                    final String key = entry.getKey();
                    final String value = entry.getValue();
                    if (key.equals("binary")) {
                        option.setBinary(new File(value));
                    } else if (key.startsWith("experimental.")) {
                        switch (value.toLowerCase()) {
                            case "true", "false" ->
                                    prefs.put(key.substring("experimental.".length()), Boolean.valueOf(value));
                            default -> prefs.put(key.substring("experimental.".length()), value);
                        }
                    } else if (key.startsWith("edge.arguments.")) {
                        if (!Optional.ofNullable(config.get(key)).orElse("").isBlank()) {
                            option.addArguments("--" + key.substring("edge.arguments.".length()) + "=" + config.get(key));
                        } else {
                            option.addArguments("--" + key.substring("edge.arguments.".length()));
                        }
                    } else if (key.startsWith("edge.extensions.")) {
                        option.addExtensions(new File(value));
                    } else {
                        caps.put(key, value);
                    }
                });
        option.setExperimentalOption("prefs", prefs);
        return option.merge(new DesiredCapabilities(caps));
    }

    @Override
    public void setDriverPath(final String driverPath) {
        System.setProperty("webdriver.edge.driver", driverPath);
    }

    @Override
    public String getDriverPath() {
        return System.getProperty("webdriver.edge.driver");
    }

    @Override
    public String getDriverName() {
        return "msedgedriver";
    }

}
