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
    public RemoteWebDriver createLocaleDriver(final Map<String, String> config) {
        return new InternetExplorerDriver(this.getOptions(config));
    }

    @Override
    public InternetExplorerOptions getOptions(final Map<String, String> config) {
        final HashMap<String, String> caps = new HashMap<>();
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        final InternetExplorerOptions ieOptions = new InternetExplorerOptions(capabilities)
                .attachToEdgeChrome();
        config.forEach((key, value) -> {
            if (key.startsWith("ieoption.")) {
                if (value.toLowerCase().equals(Boolean.TRUE.toString())) {
                    ieOptions.setCapability(key.substring("ieoption.".length()), true);
                } else if (value.toLowerCase().equals(Boolean.FALSE.toString())) {
                    ieOptions.setCapability(key.substring("ieoption.".length()), false);
                } else {
                    ieOptions.setCapability(key.substring("ieoption.".length()), value);
                }
            } else if (key.equals("binary")) {
                ieOptions.withEdgeExecutablePath(value);
            } else {
                caps.put(key, value);
            }
        });
        caps.forEach(ieOptions::setCapability);
        return ieOptions;
    }

    @Override
    public void setDriverPath(final String driverPath) {
        System.setProperty("webdriver.ie.driver", driverPath);
    }

    @Override
    public String getDriverPath() {
        return System.getProperty("webdriver.ie.driver");
    }

    @Override
    public String getDriverName() {
        return "IEDriverServer";
    }

}
