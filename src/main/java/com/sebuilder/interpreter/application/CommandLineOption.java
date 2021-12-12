package com.sebuilder.interpreter.application;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sebuilder.interpreter.Context;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.util.*;

public class CommandLineOption {

    private Long implicitlyWait = (long) -1;
    private Long pageLoadTimeout = (long) -1;
    private String driver = "Chrome";
    private String driverPath;
    private final Map<String, String> driverConfig = Maps.newHashMap();
    private String datasourceEncoding = "UTF-8";
    private String datasourceDirectory = "input";
    private String resultoutput = "result";
    private Context.TestNamePrefix junitReportPrefix = Context.TestNamePrefix.TIMESTAMP;
    private String downloadoutput = "download";
    private String screenshotoutput = "screenshot";
    private String templateoutput = "template";
    private String aspectFile;
    private String environmentProperties;
    private final Map<String, String> envVar = Maps.newHashMap();
    private Locale locale;
    private File localeConf;
    private final LinkedHashSet<String> scripts = Sets.newLinkedHashSet();
    @Argument
    private final List<String> arguments = Lists.newArrayList();

    public void parse(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);
        parser.stopOptionParsing();
        parser.parseArgument(args);
        this.parseArguments();
    }

    private void parseArguments() {
        for (String args : this.arguments) {
            if (args.startsWith("--")) {
                String[] kv = args.split("=", 2);
                if (kv[0].equals(CommandLineArgument.IMPLICITLY_WAIT.key())) {
                    this.implicitlyWait = Long.valueOf(kv[1]);
                } else if (kv[0].equals(CommandLineArgument.PAGE_LOAD_TIMEOUT.key())) {
                    this.pageLoadTimeout = Long.valueOf(kv[1]);
                } else if (kv[0].startsWith(CommandLineArgument.DRIVER_CONFIG_PREFIX.key())) {
                    this.driverConfig.put(kv[0].substring(CommandLineArgument.DRIVER_CONFIG_PREFIX.key().length()), kv[1]);
                } else if (kv[0].equals(CommandLineArgument.DRIVER.key())) {
                    this.driver = kv[1];
                } else if (kv[0].equals(CommandLineArgument.DRIVER_PATH.key())) {
                    this.driverPath = kv[1];
                } else if (kv[0].equals(CommandLineArgument.DATASOURCE_ENCODING.key())) {
                    this.datasourceEncoding = kv[1];
                } else if (kv[0].equals(CommandLineArgument.DATASOURCE_DIRECTORY.key())) {
                    this.datasourceDirectory = kv[1];
                } else if (kv[0].equals(CommandLineArgument.SCREENSHOT_OUTPUT.key())) {
                    this.screenshotoutput = kv[1];
                } else if (kv[0].equals(CommandLineArgument.TEMPLATE_OUTPUT.key())) {
                    this.templateoutput = kv[1];
                } else if (kv[0].equals(CommandLineArgument.RESULT_OUTPUT.key())) {
                    this.resultoutput = kv[1];
                } else if (kv[0].equals(CommandLineArgument.JUNIT_REPORT_PREFIX.key())) {
                    this.junitReportPrefix = Context.TestNamePrefix.fromName(kv[1]);
                } else if (kv[0].equals(CommandLineArgument.DOWNLOAD_OUTPUT.key())) {
                    this.downloadoutput = kv[1];
                } else if (kv[0].equals(CommandLineArgument.ASPECT.key())) {
                    this.aspectFile = kv[1];
                } else if (kv[0].equals(CommandLineArgument.ENVIRONMENT_PROPERTIES.key())) {
                    this.environmentProperties = kv[1];
                } else if (kv[0].startsWith(CommandLineArgument.ENVIRONMENT_PROPERTIES_PREFIX.key())) {
                    this.envVar.put(kv[0].substring(CommandLineArgument.ENVIRONMENT_PROPERTIES_PREFIX.key().length()), kv[1]);
                } else if (kv[0].equals(CommandLineArgument.LOCALE_CONF.key())) {
                    this.localeConf = new File(kv[1]);
                } else if (kv[0].equals(CommandLineArgument.LOCALE.key())) {
                    this.locale = Locale.forLanguageTag(kv[1]);
                }
            } else {
                scripts.add(args);
            }
        }
    }

    public Long getImplicitlyWait() {
        return implicitlyWait;
    }

    public Long getPageLoadTimeout() {
        return pageLoadTimeout;
    }

    public String getDriver() {
        return driver;
    }

    public String getDriverPath() {
        return driverPath;
    }

    public Map<String, String> getDriverConfig() {
        return driverConfig;
    }

    public String getDatasourceEncoding() {
        return datasourceEncoding;
    }

    public String getDatasourceDirectory() {
        return datasourceDirectory;
    }

    public String getScreenshotoutput() {
        return screenshotoutput;
    }

    public String getTemplateoutput() {
        return templateoutput;
    }

    public String getResultoutput() {
        return resultoutput;
    }

    public Context.TestNamePrefix getJunitReportPrefix() {
        return this.junitReportPrefix;
    }

    public String getDownloadoutput() {
        return downloadoutput;
    }

    public String getAspectFile() {
        return aspectFile;
    }

    public String getEnvironmentProperties() {
        return environmentProperties;
    }

    public Map<String, String> getEnvVar() {
        return envVar;
    }

    public Locale getLocale() {
        return locale;
    }

    public File getLocaleConf() {
        return localeConf;
    }

    public String getDriverConfig(String key) {
        return this.driverConfig.get(key);
    }

    public String getEnvVar(String key) {
        return this.envVar.get(key);
    }

    public Set<String> getScripts() {
        return this.scripts;
    }

}
