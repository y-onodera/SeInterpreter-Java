package com.sebuilder.interpreter.javafx.view.step;

import org.openqa.selenium.bidi.network.BytesValue;

public record HttpHeader(String key, BytesValue value) {
}
