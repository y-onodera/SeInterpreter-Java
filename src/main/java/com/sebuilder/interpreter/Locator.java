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

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * A Selenium locator.
 *
 * @author zarkonnen
 */
public class Locator {
    public Type type;
    public String value;

    static Locator of(WebDriverWrapper driver, WebElement element) {
        String id = element.getAttribute("id");
        if ("option".equals(element.getTagName())) {
            WebElement parent = element.findElement(By.xpath(".."));
            String parentAttribute = parent.getAttribute("id");
            if (parent.getTagName().equals("select") && !Strings.isNullOrEmpty(parentAttribute)) {
                String value = element.getAttribute("value");
                return new Locator("xpath", String.format("//select[@id='%s']/option[@value='%s']", parentAttribute, value));
            }
            String value = parent.getAttribute("value");
            if (!Strings.isNullOrEmpty(value)) {
                return new Locator("xpath", WebElements.toXpath(driver, element) + "[@value='" + value + "']");
            }
        }
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

    public Locator(String type, String value) {
        this.type = Type.ofName(type);
        this.value = value;
    }

    public Locator(Locator l) {
        this.type = l.type;
        this.value = l.value;
    }

    public Locator copy() {
        return new Locator(this);
    }

    public WebElement find(TestRun ctx) {
        return this.type.find(this.value, ctx);
    }

    public List<WebElement> findElements(TestRun ctx) {
        return this.type.findElements(this.value, ctx);
    }

    @Override
    public String toString() {
        return this.toPrettyString();
    }

    public String toPrettyString() {
        return this.type.name().toLowerCase() + ":" + this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locator locator = (Locator) o;
        return type == locator.type &&
                Objects.equal(value, locator.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, value);
    }

    public enum Type {
        ID {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElement(By.id(value));
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElements(By.id(value));
            }
        },
        NAME {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElement(By.name(value));
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElements(By.name(value));
            }
        },
        LINK_TEXT {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElement(By.linkText(value));
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElements(By.linkText(value));
            }
        },
        CSS_SELECTOR {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElement(By.cssSelector(value));
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElements(By.cssSelector(value));
            }
        },
        XPATH {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElement(By.xpath(value));
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElements(By.xpath(value));
            }
        };

        public abstract WebElement find(String value, TestRun ctx);

        public abstract List<WebElement> findElements(String value, TestRun ctx);

        @Override
        public String toString() {
            return this.name().toLowerCase().replace("_", " ");
        }

        public static Type ofName(String name) {
            return Type.valueOf(name.toUpperCase().replace(" ", "_"));
        }
    }
}
