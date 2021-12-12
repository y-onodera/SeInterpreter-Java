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
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);
        InternetExplorerOptions ieOptions = new InternetExplorerOptions(capabilities);
        config.forEach((key, value) -> {
            if (key.startsWith("ieoption.")) {
                if (value.toLowerCase().equals(Boolean.TRUE.toString())) {
                    ieOptions.setCapability(key.substring("ieoption.".length()), true);
                } else if (value.toLowerCase().equals(Boolean.FALSE.toString())) {
                    ieOptions.setCapability(key.substring("ieoption.".length()), false);
                } else {
                    ieOptions.setCapability(key.substring("ieoption.".length()), value);
                }
            } else {
                caps.put(key, value);
            }
        });
        caps.forEach((key,value)->ieOptions.setCapability(key,value));
        ieOptions.attachToEdgeChrome()
                .withEdgeExecutablePath(this.getBinaryPath())
                .setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "accept");
        ieOptions.setCapability("disable-popup-blocking", true);
        return new InternetExplorerDriver(ieOptions);
    }

    @Override
    public void setDriverPath(String driverPath) {
        System.setProperty("webdriver.ie.driver", driverPath);
    }

    @Override
    public String getDriverPath() {
        return System.getProperty("webdriver.ie.driver");
    }

    @Override
    public String getDriverName() {
        return "IEDriverServer.exe";
    }

    @Override
    public boolean isBinarySelectable() {
        return true;
    }
}
