package com.sebuilder.interpreter;

import java.io.File;

public class Context {
    private static Context ourInstance = new Context();

    public static Context getInstance() {
        return ourInstance;
    }

    /**
     *
     */
    private String dataSourceEncording = "UTF-8";
    /**
     *
     */
    private String dataSourceDirectory = "input";
    /**
     *
     */
    private String screenShotOutputDirectory = "screenshot";
    /**
     *
     */
    private String resultOutputDirectory = "result";
    /**
     *
     */
    private String templateOutputDirectory = "template";
    /**
     *
     */
    private String downloadDirectory = "download";

    private Context() {
    }

    /**
     * @return
     */
    public String getDataSourceEncording() {
        return dataSourceEncording;
    }

    /**
     * @param dataSourceEncording
     */
    public void setDataSourceEncording(String dataSourceEncording) {
        this.dataSourceEncording = dataSourceEncording;
    }

    /**
     * @return
     */
    public File getDataSourceDirectory() {
        return new File(dataSourceDirectory);
    }

    /**
     * @param dataSourceDirectory
     */
    public void setDataSourceDirectory(String dataSourceDirectory) {
        this.dataSourceDirectory = dataSourceDirectory;
    }

    /**
     * @return
     */
    public File getScreenShotOutputDirectory() {
        File result = new File(this.getResultOutputDirectory(), this.screenShotOutputDirectory);
        if (!result.exists()) {
            result.mkdirs();
        }
        return new File(this.getResultOutputDirectory(), screenShotOutputDirectory);
    }

    /**
     * @param screenShotOutput
     */
    public void setScreenShotOutputDirectory(String screenShotOutput) {
        this.screenShotOutputDirectory = screenShotOutput;
    }

    public File getResultOutputDirectory() {
        return new File(resultOutputDirectory);
    }

    public void setResultOutputDirectory(String aResultOutputDirectory) {
        this.resultOutputDirectory = aResultOutputDirectory;
    }

    public File getTemplateOutputDirectory() {
        File result = new File(this.getResultOutputDirectory(), this.templateOutputDirectory);
        if (!result.exists()) {
            result.mkdirs();
        }
        return result;
    }

    public void setTemplateOutputDirectory(String aTemplateOutputDirectory) {
        this.templateOutputDirectory = aTemplateOutputDirectory;
    }

    public File getDownloadDirectory() {
        File result = new File(this.getResultOutputDirectory(), this.downloadDirectory);
        if (!result.exists()) {
            result.mkdirs();
        }
        return result;
    }

    public void setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }
}
