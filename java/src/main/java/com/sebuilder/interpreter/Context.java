package com.sebuilder.interpreter;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.sebuilder.interpreter.pointcut.StepTypeFilter;
import com.sebuilder.interpreter.step.type.SaveScreenshot;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Context {

    INSTANCE;

    public static final String REMOTE_URL_KEY = "remote-url";
    private final File baseDirectory = Paths.get(".").toAbsolutePath().normalize().toFile();
    private String browser;
    private final HashMap<String, String> driverConfig = new HashMap<>();
    private WebDriverFactory wdf;
    private Long implicitlyWaitTime;
    private Long pageLoadWaitTime;
    private DataSourceFactory dataSourceFactory;
    private String dataSourceDirectory;
    private String dataSourceEncoding;
    private String resultOutputDirectory;
    private TestNamePrefix junitReportPrefix;
    private TestRunListener.Factory testRunListenerFactory;
    private String downloadDirectory;
    private String screenShotOutputDirectory;
    private String templateOutputDirectory;
    private String defaultScript = "sebuilder";
    private final Map<String, ScriptParser> scriptParsers = new HashMap<>();
    private TestCaseConverter testCaseConverter;
    private StepTypeFactory stepTypeFactory;
    private Aspect aspect = new Aspect().builder()
            .interceptor()
            .setPointcut(new StepTypeFilter(SaveScreenshot.class.getSimpleName(), "!equal"))
            .addFailure(new SaveScreenshot().toStep().put("file", "failure.png").build().toTestCase())
            .build()
            .build();
    private final Properties environmentProperties = new Properties();
    private Locale locale;
    private File localeConfDir;

    Context() {
        try {
            File envPropertyFile = new File("env.properties");
            if (envPropertyFile.exists()) {
                environmentProperties.load(new InputStreamReader(new FileInputStream(envPropertyFile), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static Context getInstance() {
        return INSTANCE;
    }

    public static InputData settings() {
        return new InputData().builder()
                .add("_browser", Context.getBrowser())
                .add("_baseDir", Context.getBaseDirectory().getAbsolutePath())
                .add("_dataSourceDir", Context.getDataSourceDirectory().getAbsolutePath())
                .add(localizeTexts())
                .add(environmentVariables())
                .build();
    }

    public static Long getImplicitlyWaitTime() {
        return getInstance().implicitlyWaitTime;
    }

    public static Long getPageLoadWaitTime() {
        return getInstance().pageLoadWaitTime;
    }

    public static String getBrowser() {
        return getInstance().browser;
    }

    public static boolean isRemote() {
        return getInstance().driverConfig.containsKey(REMOTE_URL_KEY);
    }

    public static String getRemoteUrl() {
        if (isRemote()) {
            return getInstance().driverConfig.get(REMOTE_URL_KEY);
        }
        return "";
    }

    public static Map<String, String> getDriverConfig() {
        return getInstance().driverConfig;
    }

    public static WebDriverFactory getWebDriverFactory() {
        return getInstance().wdf;
    }

    public static WebDriverFactory getWebDriverFactory(String browser) {
        try {
            String classname = browser.substring(0, 1).toUpperCase() + browser.substring(1);
            return (WebDriverFactory) Class.forName("com.sebuilder.interpreter.browser." + classname).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new AssertionError("Unknown WebDriverFactory: " + "com.sebuilder.interpreter.browser." + browser, e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError("Could not instantiate WebDriverFactory " + "com.sebuilder.interpreter.browser." + browser, e);
        }
    }

    public static ScriptParser getScriptParser() {
        return getScriptParser(getDefaultScript());
    }

    public static ScriptParser getScriptParser(String scriptType) {
        return getInstance().scriptParsers.get(scriptType);
    }

    public static TestCaseConverter getTestCaseConverter() {
        return getInstance().testCaseConverter;
    }

    public static StepTypeFactory getStepTypeFactory() {
        return getInstance().stepTypeFactory;
    }

    public static DataSourceFactory getDataSourceFactory() {
        return getInstance().dataSourceFactory;
    }

    public static String getDefaultScript() {
        return getInstance().defaultScript;
    }

    public static File getBaseDirectory() {
        return getInstance().baseDirectory;
    }

    public static File getDataSourceDirectory() {
        return new File(getInstance().dataSourceDirectory);
    }

    public static String getDataSourceEncoding() {
        return getInstance().dataSourceEncoding;
    }

    public static File getResultOutputDirectory() {
        return new File(getInstance().resultOutputDirectory);
    }

    public static String getJunitReportPrefix() {
        switch (getInstance().junitReportPrefix) {
            case TIMESTAMP:
                return "start" + DateTimeFormatter
                        .ofPattern("yyyyMMddHHmmss")
                        .format(LocalDateTime.now()) + ".";
            case RESULT_DIR:
                return getInstance().resultOutputDirectory
                        .replace("/", "_")
                        .replace("\\", "_") + ".";
        }
        return "";
    }

    public static String getDownloadDirectory() {
        return getInstance().downloadDirectory;
    }

    public static String getScreenShotOutputDirectory() {
        return getInstance().browser + "_" + getInstance().screenShotOutputDirectory;
    }

    public static String getTemplateOutputDirectory() {
        return getInstance().templateOutputDirectory;
    }

    public static Aspect getAspect() {
        return getInstance().aspect;
    }

    public static Properties getEnvironmentProperties() {
        return getInstance().environmentProperties;
    }

    public static File getExpectScreenShotDirectory() {
        return new File(getDataSourceDirectory(), getScreenShotOutputDirectory());
    }

    public static String bindEnvironmentProperties(String variable) {
        for (Map.Entry<Object, Object> v : getInstance().environmentProperties.entrySet()) {
            variable = variable.replace("${env." + v.getKey().toString() + "}", v.getValue().toString());
        }
        return variable;
    }

    public static TestRunListener getTestListener(Logger log) {
        return getInstance().testRunListenerFactory.create(log);
    }

    public Context ifMatch(boolean condition, Function<Context, Context> modifier) {
        if (condition) {
            return modifier.apply(this);
        }
        return this;
    }

    public Context setImplicitlyWaitTime(Long aLong) {
        this.implicitlyWaitTime = aLong;
        return this;
    }

    public Context setPageLoadWaitTime(Long aLong) {
        this.pageLoadWaitTime = aLong;
        return this;
    }

    public void setBrowser(String browserName, String driverPath, String binaryPath) {
        this.setBrowser(browserName, driverPath).wdf.setBinaryPath(binaryPath);
    }

    public Context setBrowser(String browserName, String driverPath) {
        return this.setBrowser(browserName)
                .setWebDriverPath(driverPath);
    }

    public Context setBrowser(String browser) {
        return setWebDriverFactory(getWebDriverFactory(browser));
    }

    public Context setRemoteUrl(String remoteUrl) {
        if (Strings.isNullOrEmpty(remoteUrl)) {
            this.driverConfig.remove(REMOTE_URL_KEY);
        } else {
            this.driverConfig.put(REMOTE_URL_KEY, remoteUrl);
        }
        return this;
    }

    public Context setWebDriverPath(String driverPath) {
        this.wdf.setDriverPath(driverPath);
        return this;
    }

    public Context setWebDriverFactory(WebDriverFactory webDriverFactory) {
        this.wdf = webDriverFactory;
        this.browser = webDriverFactory.targetBrowser();
        return this;
    }

    public Context setDriverConfig(Map<String, String> config) {
        this.driverConfig.putAll(config);
        return this;
    }


    public Context addScriptParser(ScriptParser parser) {
        this.scriptParsers.put(parser.type(), parser);
        return this;
    }

    public Context setDefaultScriptParser(ScriptParser parser) {
        this.defaultScript = parser.type();
        return this.addScriptParser(parser);
    }

    public Context setTestCaseConverter(TestCaseConverter converter) {
        this.testCaseConverter = converter;
        return this;
    }

    public Context setStepTypeFactory(StepTypeFactory stepTypeFactory) {
        this.stepTypeFactory = stepTypeFactory;
        return this;
    }

    public Context setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        return this;
    }

    public Context setDataSourceDirectory(String dataSourceDirectory) {
        this.dataSourceDirectory = dataSourceDirectory;
        return this;
    }

    public Context setDataSourceEncoding(String dataSourceEncoding) {
        this.dataSourceEncoding = dataSourceEncoding;
        return this;
    }

    public Context setResultOutputDirectory(String aResultOutputDirectory) {
        this.resultOutputDirectory = aResultOutputDirectory;
        return this;
    }

    public Context setJunitReportPrefix(TestNamePrefix junitReportPrefix) {
        this.junitReportPrefix = junitReportPrefix;
        return this;
    }

    public Context setTestRunListenerFactory(TestRunListener.Factory testRunListenerFactory) {
        this.testRunListenerFactory = testRunListenerFactory;
        return this;
    }

    public Context setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
        return this;
    }

    public Context setScreenShotOutputDirectory(String screenShotOutput) {
        this.screenShotOutputDirectory = screenShotOutput;
        return this;
    }

    public Context setTemplateOutputDirectory(String aTemplateOutputDirectory) {
        this.templateOutputDirectory = aTemplateOutputDirectory;
        return this;
    }

    public Context setAspect(String aspectFileName) {
        try {
            this.aspect = getScriptParser().loadAspect(new File(getBaseDirectory(), aspectFileName));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return this;
    }

    public Context setAspect(Aspect aAspect) {
        this.aspect = aAspect;
        return this;
    }

    public Context setEnvironmentProperties(String propertyFile) {
        this.environmentProperties.clear();
        try (FileInputStream is = new FileInputStream(propertyFile)) {
            this.environmentProperties.load(is);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return this;
    }

    public Context setEnvironmentProperty(Map<String, String> property) {
        this.environmentProperties.putAll(property);
        return this;
    }

    public Context setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public Context setLocaleConfDir(File path) {
        this.localeConfDir = path;
        return this;
    }

    private static Map<String, String> environmentVariables() {
        return getInstance().environmentProperties
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        it -> "env." + it.getKey()
                        , it -> it.getValue().toString()
                        , (e1, e2) -> e1
                        , HashMap::new));
    }

    private static Map<String, String> localizeTexts() {
        File loadFrom = Optional.ofNullable(getInstance().localeConfDir).orElse(getInstance().baseDirectory);
        Locale currentLocale = Optional.ofNullable(getInstance().locale).orElse(Locale.getDefault());
        try (URLClassLoader url = new URLClassLoader(new URL[]{loadFrom.toURI().toURL()})) {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("locale", currentLocale, url);
            return resourceBundle.keySet()
                    .stream()
                    .collect(Collectors.toMap(
                            it -> "locale." + it
                            , resourceBundle::getString
                            , (e1, e2) -> e1
                            , HashMap::new));
        } catch (IOException e) {
            return Maps.newHashMap();
        }
    }

    public enum TestNamePrefix {
        TIMESTAMP("timestamp"), RESULT_DIR("resultDir"), NONE("none");
        private final String name;

        TestNamePrefix(String name) {
            this.name = name;
        }

        public static TestNamePrefix fromName(String name) {
            return Stream.of(TestNamePrefix.values())
                    .filter(it -> it.name.equals(name))
                    .findFirst()
                    .get();
        }
    }

}
