package com.sebuilder.interpreter;

import com.sebuilder.interpreter.pointcut.TypeFilter;
import com.sebuilder.interpreter.step.type.SaveScreenshot;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.manager.SeleniumManager;
import org.openqa.selenium.manager.SeleniumManagerOutput;

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

    public static final String BROWSER_BINARY_KEY = "binary";
    public static final String BROWSER_VERSION_KEY = "browserVersion";
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
    private ReportPrefix reportPrefix;
    private TestRunListener.Factory testRunListenerFactory;
    private String downloadDirectory;
    private String screenShotOutputDirectory;
    private String expectScreenShotDirectory;
    private String templateOutputDirectory;
    private String defaultScript = "sebuilder";
    private final Map<String, ScriptParser> scriptParsers = new HashMap<>();
    private TestCaseConverter testCaseConverter;
    private StepTypeFactory stepTypeFactory;
    private Aspect aspect = new Aspect().builder()
            .add(new ExtraStepExecutor
                    .Builder()
                    .setDisplayName("default")
                    .setPointcut(new TypeFilter(SaveScreenshot.class.getSimpleName(), "!equals"))
                    .addFailure(new SaveScreenshot().toStep().put("file", "failure.png").build().toTestCase())
                    .build()
            )
            .build();
    private final Properties environmentProperties = new Properties();
    private Locale locale;
    private File localeConfDir;
    private int waitForMaxMs;
    private int waitForIntervalMs;

    Context() {
        try {
            final File envPropertyFile = new File("env.properties");
            if (envPropertyFile.exists()) {
                this.environmentProperties.load(new InputStreamReader(new FileInputStream(envPropertyFile), StandardCharsets.UTF_8));
            }
        } catch (final IOException e) {
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

    public static int getWaitForMaxMs() {
        return getInstance().waitForMaxMs;
    }

    public static int getWaitForIntervalMs() {
        return getInstance().waitForIntervalMs;
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

    public static WebDriverFactory getWebDriverFactory(final String browser) {
        try {
            final String classname = browser.substring(0, 1).toUpperCase() + browser.substring(1);
            return (WebDriverFactory) Class.forName("com.sebuilder.interpreter.browser." + classname).getDeclaredConstructor().newInstance();
        } catch (final ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new AssertionError("Unknown WebDriverFactory: " + "com.sebuilder.interpreter.browser." + browser, e);
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new AssertionError("Could not instantiate WebDriverFactory " + "com.sebuilder.interpreter.browser." + browser, e);
        }
    }

    public static ScriptParser getScriptParser() {
        return getScriptParser(getDefaultScript());
    }

    public static ScriptParser getScriptParser(final String scriptType) {
        return getInstance().scriptParsers.get(scriptType);
    }

    public static TestCaseConverter getTestCaseConverter() {
        return getInstance().testCaseConverter;
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

    public static TestRunListener.Factory getTestRunListenerFactory() {
        return getInstance().testRunListenerFactory;
    }

    public static String getReportPrefixValue() {
        return switch (getReportPrefix()) {
            case TIMESTAMP -> "start" + DateTimeFormatter
                    .ofPattern("yyyyMMddHHmmss")
                    .format(LocalDateTime.now()) + ".";
            case RESULT_DIR -> getInstance().resultOutputDirectory
                    .replace("/", "_")
                    .replace("\\", "_") + ".";
            default -> "";
        };
    }

    public static ReportPrefix getReportPrefix() {
        return getInstance().reportPrefix;
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
        if (getInstance().expectScreenShotDirectory == null || getInstance().expectScreenShotDirectory.isBlank()) {
            return new File(getDataSourceDirectory(), getScreenShotOutputDirectory());
        }
        return new File(getInstance().expectScreenShotDirectory);
    }

    public static String bindEnvironmentProperties(String variable) {
        for (final Map.Entry<Object, Object> v : getInstance().environmentProperties.entrySet()) {
            variable = variable.replace("${env." + v.getKey().toString() + "}", v.getValue().toString());
        }
        return variable;
    }

    public static TestRunListener getTestListener(final Logger log) {
        return getInstance().testRunListenerFactory.create(log);
    }

    public static StepType getStepTypeOfName(final String stepType) {
        return getInstance().stepTypeFactory.getStepTypeOfName(stepType);
    }

    public static StepBuilder createStepBuilder(final String stepType) {
        return new StepBuilder(getStepTypeOfName(stepType));
    }

    public static Step createStep(final String stepType) {
        return getStepTypeOfName(stepType)
                .toStep()
                .build()
                .toTestCase()
                .steps().get(0);
    }

    public static String toString(final Suite suite) {
        return getTestCaseConverter().toString(suite);
    }

    public static String toString(final TestCase displayTestCase) {
        return getTestCaseConverter().toString(displayTestCase);
    }

    public static String toString(final Aspect interceptors) {
        return getTestCaseConverter().toString(interceptors);
    }

    public static TestCase load(final File file) {
        return getScriptParser().load(file);
    }

    public static TestCase load(final String text, final File toFile) {
        return getScriptParser().load(text, toFile);
    }

    public static TestCase loadWithScriptType(final String scriptType, final File file) {
        return getScriptParser(scriptType).load(file);
    }

    public Context ifMatch(final boolean condition, final Function<Context, Context> modifier) {
        if (condition) {
            return modifier.apply(this);
        }
        return this;
    }

    public Context setEnvProperties(final InputData result) {
        this.environmentProperties.clear();
        this.environmentProperties.putAll(result.row());
        return this;
    }

    public Context setImplicitlyWaitTime(final Long aLong) {
        this.implicitlyWaitTime = aLong;
        return this;
    }

    public Context setPageLoadWaitTime(final Long aLong) {
        this.pageLoadWaitTime = aLong;
        return this;
    }

    public Context setWaitForMaxMs(final int waitForMaxMs) {
        this.waitForMaxMs = waitForMaxMs;
        return this;
    }

    public Context setWaitForIntervalMs(final int waitForIntervalMs) {
        this.waitForIntervalMs = waitForIntervalMs;
        return this;
    }

    public void setBrowser(final String browserName, final String driverPath, final String binaryPath) {
        this.wdf.setBinaryPath(binaryPath);
        this.setBrowser(browserName)
                .setWebDriverPath(driverPath);
    }

    public Context setBrowser(final String browser) {
        return this.setWebDriverFactory(getWebDriverFactory(browser));
    }

    public Context setRemoteUrl(final String remoteUrl) {
        if (this.isNullOrEmpty(remoteUrl)) {
            this.driverConfig.remove(REMOTE_URL_KEY);
        } else {
            this.driverConfig.put(REMOTE_URL_KEY, remoteUrl);
        }
        return this;
    }

    public Context setWebDriverPath(final String driverPath) {
        if (this.isNullOrEmpty(driverPath)) {
            final SeleniumManagerOutput.Result mangerResult = SeleniumManager.getInstance()
                    .getDriverPath(this.wdf.getOptions(this.driverConfig), false);
            this.wdf.setDriverPath(mangerResult.getDriverPath());
            this.wdf.setBinaryPath(mangerResult.getBrowserPath());
        } else {
            this.wdf.setDriverPath(driverPath);
        }
        return this;
    }

    public Context setWebDriverFactory(final WebDriverFactory webDriverFactory) {
        this.wdf = webDriverFactory;
        this.browser = webDriverFactory.targetBrowser();
        return this;
    }

    public Context setDriverConfig(final Map<String, String> config) {
        this.driverConfig.clear();
        this.driverConfig.putAll(config);
        return this;
    }


    public Context addScriptParser(final ScriptParser parser) {
        this.scriptParsers.put(parser.type(), parser);
        return this;
    }

    public Context setDefaultScriptParser(final ScriptParser parser) {
        this.defaultScript = parser.type();
        return this.addScriptParser(parser);
    }

    public Context setTestCaseConverter(final TestCaseConverter converter) {
        this.testCaseConverter = converter;
        return this;
    }

    public Context setStepTypeFactory(final StepTypeFactory stepTypeFactory) {
        this.stepTypeFactory = stepTypeFactory;
        return this;
    }

    public Context setDataSourceFactory(final DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        return this;
    }

    public Context setDataSourceDirectory(final String dataSourceDirectory) {
        this.dataSourceDirectory = dataSourceDirectory;
        return this;
    }

    public Context setDataSourceEncoding(final String dataSourceEncoding) {
        this.dataSourceEncoding = dataSourceEncoding;
        return this;
    }

    public Context setResultOutputDirectory(final String aResultOutputDirectory) {
        this.resultOutputDirectory = aResultOutputDirectory;
        return this;
    }

    public Context setReportPrefix(final ReportPrefix reportPrefix) {
        this.reportPrefix = reportPrefix;
        return this;
    }

    public Context setTestRunListenerFactory(final TestRunListener.Factory testRunListenerFactory) {
        this.testRunListenerFactory = testRunListenerFactory;
        return this;
    }

    public Context setDownloadDirectory(final String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
        return this;
    }

    public Context setScreenShotOutputDirectory(final String screenShotOutput) {
        this.screenShotOutputDirectory = screenShotOutput;
        return this;
    }

    public Context setExpectScreenShotDirectory(final String expectScreenShot) {
        this.expectScreenShotDirectory = expectScreenShot;
        return this;
    }

    public Context setTemplateOutputDirectory(final String aTemplateOutputDirectory) {
        this.templateOutputDirectory = aTemplateOutputDirectory;
        return this;
    }

    public Context setAspect(final String aspectFileName) {
        this.aspect = getScriptParser().loadAspect(new File(getBaseDirectory(), aspectFileName));
        return this;
    }

    public Context setAspect(final Aspect aAspect) {
        this.aspect = aAspect;
        return this;
    }

    public Context setEnvironmentProperties(final String propertyFile) {
        this.environmentProperties.clear();
        try (final FileInputStream is = new FileInputStream(propertyFile)) {
            this.environmentProperties.load(is);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        return this;
    }

    public Context setEnvironmentProperty(final Map<String, String> property) {
        this.environmentProperties.putAll(property);
        return this;
    }

    public Context setLocale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    public Context setLocaleConfDir(final File path) {
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
        final File loadFrom = Optional.ofNullable(getInstance().localeConfDir).orElse(getInstance().baseDirectory);
        final Locale currentLocale = Optional.ofNullable(getInstance().locale).orElse(Locale.getDefault());
        try (final URLClassLoader url = new URLClassLoader(new URL[]{loadFrom.toURI().toURL()})) {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("locale", currentLocale, url);
            return resourceBundle.keySet()
                    .stream()
                    .collect(Collectors.toMap(
                            it -> "locale." + it
                            , resourceBundle::getString
                            , (e1, e2) -> e1
                            , HashMap::new));
        } catch (final IOException e) {
            return new HashMap<>();
        }
    }


    public enum ReportPrefix {
        TIMESTAMP("timestamp"), RESULT_DIR("resultDir"), NONE("none");
        private final String name;

        ReportPrefix(final String name) {
            this.name = name;
        }

        public static ReportPrefix fromName(final String name) {
            return Stream.of(ReportPrefix.values())
                    .filter(it -> it.name.equals(name))
                    .findFirst()
                    .orElse(NONE);
        }

        public String getName() {
            return this.name;
        }
    }

    private boolean isNullOrEmpty(final String driverPath) {
        return Optional.ofNullable(driverPath).filter(it -> !it.isEmpty()).isEmpty();
    }

}
