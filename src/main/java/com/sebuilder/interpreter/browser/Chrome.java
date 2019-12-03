package com.sebuilder.interpreter.browser;

import com.sebuilder.interpreter.WebDriverFactory;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Chrome implements WebDriverFactory {

    /**
     * @param config A key/value mapping of configuration options specific to this factory.
     * @return A RemoteWebDriver of the type produced by this factory.
     */
    @Override
    public RemoteWebDriver make(Map<String, String> config) {
        HashMap<String, String> caps = new HashMap<String, String>();
        HashMap<String, String> prefs = new HashMap<String, String>();
        ChromeOptions option = new ChromeOptions();
        if (config.containsKey("binary")) {
            option.setBinary(new File(config.get("binary")));
        }
        config.forEach((key, value) -> {
            if (key.startsWith("experimental.")) {
                prefs.put(key.substring("experimental.".length()), value);
            } else if (key.startsWith("chrome.arguments.")) {
                option.addArguments("--" + key.substring("chrome.arguments.".length()));
            } else {
                caps.put(key, value);
            }
        });
        option.setExperimentalOption("prefs", prefs);
        DesiredCapabilities capabilities = new DesiredCapabilities(caps);
        return new ChromeDriver(option.merge(capabilities));
    }

    @Override
    public void setDriverPath(String driverPath) {
        System.setProperty("webdriver.chrome.driver", driverPath);
    }

    @Override
    public String getDriverPath() {
        return System.getProperty("webdriver.chrome.driver");
    }
}
