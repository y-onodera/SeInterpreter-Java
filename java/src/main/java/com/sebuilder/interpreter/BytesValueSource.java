package com.sebuilder.interpreter;

import org.openqa.selenium.bidi.network.BytesValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public record BytesValueSource(BytesValue.Type type, String value, String filePath, boolean needEncoding) {

    public BytesValue byteValue() {
        if (this.type == BytesValue.Type.STRING) {
            return new BytesValue(this.type, this.value);
        }
        String resolvedValue = this.value;
        if (Strings.isNotEmpty(this.filePath)) {
            try {
                resolvedValue = String.join("", Files.readAllLines(Path.of(filePath()), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (needEncoding()) {
            resolvedValue = Base64.getEncoder().encodeToString(resolvedValue.getBytes(StandardCharsets.UTF_8));
        }
        return new BytesValue(type, resolvedValue);
    }

    public String toPrettyString() {
        return "[" +
                "type=" + type.name() +
                ", value=" + value +
                ", filePath=" + filePath +
                ", needEncoding=" + needEncoding +
                "]";
    }
}
