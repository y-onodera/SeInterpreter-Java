package com.sebuilder.interpreter.webdriverfactory;

import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;

public class InternetExplorer implements WebDriverFactory {
    /**
     * @param config A key/value mapping of configuration options specific to this factory.
     * @return A RemoteWebDriver of the type produced by this factory.
     */
    @Override
    public RemoteWebDriver make(HashMap<String, String> config) {
        DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
        ieCapabilities.setCapability("nativeEvents", false);
        ieCapabilities.setCapability("unexpectedAlertBehaviour", "accept");
        ieCapabilities.setCapability("ignoreProtectedModeSettings", true);
        ieCapabilities.setCapability("disable-popup-blocking", true);
        ieCapabilities.setCapability("enablePersistentHover", true);
        ieCapabilities.setCapability("ignoreZoomSetting", true);
        HashMap<String, String> caps = new HashMap<String, String>(config);
        DesiredCapabilities capabilities = new DesiredCapabilities(caps);
        ieCapabilities.merge(capabilities);
        return new InternetExplorerDriver(new InternetExplorerOptions(ieCapabilities));
    }

    @Override
    public void setDriverPath(String driverPath) {
        System.setProperty("webdriver.ie.driver", driverPath);
    }
}
