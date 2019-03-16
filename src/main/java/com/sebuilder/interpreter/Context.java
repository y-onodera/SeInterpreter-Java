package com.sebuilder.interpreter;

import java.io.File;
import java.nio.file.Paths;

public enum Context {
    INSTANCE;

    public static Context getInstance() {
        return INSTANCE;
    }

    private final File baseDirectory = Paths.get(".").toAbsolutePath().normalize().toFile();
    private String dataSourceDirectory = "input";
    private String dataSourceEncoding = "UTF-8";
    private String resultOutputDirectory = "result";
    private String downloadDirectory = "download";
    private String screenShotOutputDirectory = "screenshot";
    private String templateOutputDirectory = "template";
    private String browser = "Chrome";

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public void setDataSourceDirectory(String dataSourceDirectory) {
        this.dataSourceDirectory = dataSourceDirectory;
    }

    public void setDataSourceEncoding(String dataSourceEncoding) {
        this.dataSourceEncoding = dataSourceEncoding;
    }

    public void setResultOutputDirectory(String aResultOutputDirectory) {
        this.resultOutputDirectory = aResultOutputDirectory;
    }

    public void setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }

    public void setScreenShotOutputDirectory(String screenShotOutput) {
        this.screenShotOutputDirectory = screenShotOutput;
    }

    public void setTemplateOutputDirectory(String aTemplateOutputDirectory) {
        this.templateOutputDirectory = aTemplateOutputDirectory;
    }

    public String getBrowser() {
        return this.browser;
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public File getDataSourceDirectory() {
        return new File(dataSourceDirectory);
    }

    public String getDataSourceEncoding() {
        return dataSourceEncoding;
    }

    public File getResultOutputDirectory() {
        return new File(resultOutputDirectory);
    }

    public String getDownloadDirectory() {
        return this.downloadDirectory;
    }

    public String getScreenShotOutputDirectory() {
        return this.screenShotOutputDirectory;
    }

    public String getTemplateOutputDirectory() {
        return this.templateOutputDirectory;
    }

}
