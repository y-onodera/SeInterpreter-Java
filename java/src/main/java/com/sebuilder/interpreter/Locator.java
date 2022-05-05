/*
 * Copyright 2012 Sauce Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebuilder.interpreter;

import com.google.common.base.Strings;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * A Selenium locator.
 *
 * @author zarkonnen
 */
public record Locator(String type, String value) {

    static Locator of(WebDriverWrapper driver, WebElement element) {
        String id = element.getAttribute("id");
        if (!Strings.isNullOrEmpty(id)) {
            return new Locator("id", id);
        } else if ("a".equals(element.getTagName())) {
            return new Locator("link text", element.getText());
        }
        String name = element.getAttribute("name");
        if (!Strings.isNullOrEmpty(name)) {
            return new Locator("name", name);
        }
        return new Locator("xpath", WebElements.toXpath(driver, element));
    }

    public Locator(Locator l) {
        this(l.type, l.value);
    }

    public Locator copy() {
        return new Locator(this);
    }

    public WebElement find(TestRun ctx) {
        return this.find(ctx.driver());
    }

    public WebElement find(WebDriver driver) {
        return Type.ofName(this.type).find(this.value, driver);
    }

    public List<WebElement> findElements(TestRun ctx) {
        return this.findElements(ctx.driver());
    }

    public List<WebElement> findElements(WebDriver driver) {
        return Type.ofName(this.type).findElements(this.value, driver);
    }

    @Override
    public String toString() {
        return this.toPrettyString();
    }

    public String toPrettyString() {
        return this.type + ":" + this.value;
    }

    public enum Type {
        ID {
            @Override
            public WebElement find(String value, WebDriver driver) {
                return driver.findElement(By.id(value));
            }

            @Override
            public List<WebElement> findElements(String value, WebDriver driver) {
                return driver.findElements(By.id(value));
            }
        },
        NAME {
            @Override
            public WebElement find(String value, WebDriver driver) {
                return driver.findElement(By.name(value));
            }

            @Override
            public List<WebElement> findElements(String value, WebDriver driver) {
                return driver.findElements(By.name(value));
            }
        },
        LINK_TEXT {
            @Override
            public WebElement find(String value, WebDriver driver) {
                return driver.findElement(By.linkText(value));
            }

            @Override
            public List<WebElement> findElements(String value, WebDriver driver) {
                return driver.findElements(By.linkText(value));
            }
        },
        CSS_SELECTOR {
            @Override
            public WebElement find(String value, WebDriver driver) {
                return driver.findElement(By.cssSelector(value));
            }

            @Override
            public List<WebElement> findElements(String value, WebDriver driver) {
                return driver.findElements(By.cssSelector(value));
            }
        },
        XPATH {
            @Override
            public WebElement find(String value, WebDriver driver) {
                return driver.findElement(By.xpath(value));
            }

            @Override
            public List<WebElement> findElements(String value, WebDriver driver) {
                return driver.findElements(By.xpath(value));
            }
        };

        public abstract WebElement find(String value, WebDriver driver);

        public abstract List<WebElement> findElements(String value, WebDriver driver);

        @Override
        public String toString() {
            return this.name().toLowerCase().replace("_", " ");
        }

        public static Type ofName(String name) {
            return Type.valueOf(name.toUpperCase().replace(" ", "_"));
        }
    }
}
