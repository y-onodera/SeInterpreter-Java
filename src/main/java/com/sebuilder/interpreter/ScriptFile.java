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
    private final Type type;

    public static ScriptFile of(File file, Type type) {
        if (file != null) {
            return new ScriptFile(file, type);
        }
        return new ScriptFile(type.getDefaultName(), type);
    }

    public static ScriptFile of(File file, String defaultName, Type type) {
        if (file != null) {
            return new ScriptFile(file, type);
        }
        return new ScriptFile(defaultName, type);
    }

    public ScriptFile(Type type) {
        this(type.getDefaultName(), type);
    }

    public ScriptFile(String name, Type type) {
        this(name, null, null, type);
    }

    public ScriptFile(File file, Type type) {
        this(file.getName(), file.getAbsolutePath(), file.getParentFile().getAbsoluteFile(), type);
    }

    public ScriptFile(String name, String path, File relativePath, Type type) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath;
        this.type = type;
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

    public Type type() {
        return type;
    }

    public File toFile() {
        return new File(this.path);
    }

    public String relativize(TestCase s) {
        if (this.relativePath == null && !Strings.isNullOrEmpty(s.path())) {
            return s.path();
        } else if (Strings.isNullOrEmpty(s.path())) {
            return s.name();
        }
        return this.relativePath.toPath().relativize(Paths.get(s.path()).toAbsolutePath()).toString().replace("\\", "/");
    }

    public ScriptFile changeName(String name) {
        return new ScriptFile(name, this.path, this.relativePath, this.type);
    }

    @Override
    public String toString() {
        return "ScriptFile{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", relativePath=" + relativePath +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptFile that = (ScriptFile) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(path, that.path) &&
                Objects.equal(relativePath, that.relativePath) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, path, relativePath, type);
    }

    public enum Type {
        SUITE("New_Suite"), TEST("New_Script");

        private final String defaultName;

        Type(String defaultName) {
            this.defaultName = defaultName;
        }

        public String getDefaultName() {
            return this.defaultName;
        }
    }
}