package com.sebuilder.interpreter;

import com.google.common.base.Strings;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

public class TestCase {
    private final String name;
    private final String path;
    private final File relativePath;

    public static TestCase of(File suiteFile, String defaultName) {
        if (suiteFile != null) {
            return new TestCase(suiteFile);
        }
        return new TestCase(defaultName);
    }

    public TestCase(String name) {
        this(name, null, null);
    }

    public TestCase(File suiteFile) {
        this(suiteFile.getName(), suiteFile.getAbsolutePath(), suiteFile.getParentFile().getAbsoluteFile());
    }

    public TestCase(String name, String path, File relativePath) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath;
    }

    public TestCase changeName(String name) {
        return new TestCase(name, this.path, this.relativePath);
    }

    public String nameExcludeExtention() {
        if (this.path != null && this.name().contains(".")) {
            return this.name().substring(0, this.name().lastIndexOf("."));
        }
        return this.name();
    }

    public String name() {
        return this.name;
    }

    public String path() {
        return Optional.ofNullable(this.path).orElse("");
    }

    public File relativePath() {
        return this.relativePath;
    }

    public String relativePath(Script s) {
        if (this.relativePath == null && !Strings.isNullOrEmpty(s.path())) {
            return s.path();
        } else if (Strings.isNullOrEmpty(s.path())) {
            return "script/" + s.name();
        }
        return this.relativePath.toPath().relativize(Paths.get(s.path()).toAbsolutePath()).toString().replace("\\", "/");
    }

}