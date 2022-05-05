package com.sebuilder.interpreter;

import com.google.common.base.Strings;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

public record ScriptFile(String name, String path, File relativePath, Type type) {

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
        if (Strings.isNullOrEmpty(this.path)) {
            return null;
        }
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