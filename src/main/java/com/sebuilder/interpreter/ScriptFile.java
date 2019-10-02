package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

public class ScriptFile {
    private final String name;
    private final String path;
    private final File relativePath;

    public static ScriptFile of(File file, String defaultName) {
        if (file != null) {
            return new ScriptFile(file);
        }
        return new ScriptFile(defaultName);
    }

    public ScriptFile(String name) {
        this(name, null, null);
    }

    public ScriptFile(File file) {
        this(file.getName(), file.getAbsolutePath(), file.getParentFile().getAbsoluteFile());
    }

    public ScriptFile(String name, String path, File relativePath) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath;
    }

    public ScriptFile changeName(String name) {
        return new ScriptFile(name, this.path, this.relativePath);
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

    public String relativePath(TestRunnable s) {
        if (this.relativePath == null && !Strings.isNullOrEmpty(s.path())) {
            return s.path();
        } else if (Strings.isNullOrEmpty(s.path())) {
            return "script/" + s.name();
        }
        return this.relativePath.toPath().relativize(Paths.get(s.path()).toAbsolutePath()).toString().replace("\\", "/");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptFile that = (ScriptFile) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(path, that.path) &&
                Objects.equal(relativePath, that.relativePath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, path, relativePath);
    }
}