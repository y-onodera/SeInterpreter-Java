package com.sebuilder.interpreter.browser;

import com.sebuilder.interpreter.WebDriverFactory;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Edge implements WebDriverFactory {
    @Override
    public RemoteWebDriver createLocaleDriver(Map<String, String> config) {
        return new EdgeDriver(this.getOptions(config));
    }

    @Override
    public EdgeOptions getOptions(Map<String, String> config) {
        HashMap<String, String> caps = new HashMap<>();
        HashMap<String, String> prefs = new HashMap<>();
        EdgeOptions option = new EdgeOptions();
        if (config.containsKey("binary")) {
            option.setBinary(new File(config.get("binary")));
        }
        config.forEach((key, value) -> {
            if (key.startsWith("experimental.")) {
                prefs.put(key.substring("experimental.".length()), value);
            } else if (key.startsWith("edge.arguments.")) {
                option.addArguments("--" + key.substring("edge.arguments.".length()));
            } else {
                caps.put(key, value);
            }
        });
        option.setExperimentalOption("prefs", prefs);
        return option.merge(new DesiredCapabilities(caps));
    }

    @Override
    public void setDriverPath(String driverPath) {
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

    @Override
    public boolean isBinarySelectable() {
        return true;
    }
}
