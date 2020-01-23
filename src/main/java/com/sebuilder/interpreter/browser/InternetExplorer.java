package com.sebuilder.interpreter.browser;

import com.sebuilder.interpreter.WebDriverFactory;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;
import java.util.Map;

public class InternetExplorer implements WebDriverFactory {
    /**
     * @param config A key/value mapping of configuration options specific to this factory.
     * @return A RemoteWebDriver of the type produced by this factory.
     */
    @Override
    public RemoteWebDriver make(Map<String, String> config) {
        HashMap<String, String> caps = new HashMap<>();
        HashMap<String, Object> ieOptions = new HashMap<>();
        config.forEach((key, value) -> {
            if (key.startsWith("ieoption.")) {
                if (value.toLowerCase().equals(Boolean.TRUE.toString())) {
                    ieOptions.put(key.substring("ieoption.".length()), true);
                } else if (value.toLowerCase().equals(Boolean.FALSE.toString())) {
                    ieOptions.put(key.substring("ieoption.".length()), false);
                } else {
                    ieOptions.put(key.substring("ieoption.".length()), value);
                }
            } else {
                caps.put(key, value);
            }
        });
        DesiredCapabilities capabilities = new DesiredCapabilities(caps);
        if (ieOptions.size() > 0) {
            ieOptions.put("nativeEvents", false);
            ieOptions.put("unexpectedAlertBehaviour", "accept");
            ieOptions.put("ignoreProtectedModeSettings", true);
            ieOptions.put("disable-popup-blocking", true);
            ieOptions.put("enablePersistentHover", true);
            ieOptions.put("ignoreZoomSetting", true);
            capabilities.setCapability("se:ieOptions", ieOptions);
        } else {
            capabilities.setCapability("nativeEvents", false);
            capabilities.setCapability("unexpectedAlertBehaviour", "accept");
            capabilities.setCapability("ignoreProtectedModeSettings", true);
            capabilities.setCapability("disable-popup-blocking", true);
            capabilities.setCapability("enablePersistentHover", true);
            capabilities.setCapability("ignoreZoomSetting", true);
        }
        return new InternetExplorerDriver(new InternetExplorerOptions(capabilities));
    }

    @Override
    public void setDriverPath(String driverPath) {
        System.setProperty("webdriver.ie.driver", driverPath);
    }

    @Override
    public String getDriverPath() {
        return System.getProperty("webdriver.ie.driver");
    }
}
