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
import com.google.common.collect.Lists;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;

/**
 * A Selenium locator.
 *
 * @author zarkonnen
 */
public class Locator {
    public Type type;
    public String value;

    public static Locator of(RemoteWebDriver driver, WebElement element) {
        String id = element.getAttribute("id");
        if ("option".equals(element.getTagName())) {
            WebElement parent = element.findElement(By.xpath(".."));
            String parentAttribute = parent.getAttribute("id");
            if (parent.getTagName().equals("select") && !Strings.isNullOrEmpty(parentAttribute)) {
                String value = element.getAttribute("value");
                return new Locator("xpath", String.format("//select[@id='%s']/option[@value='%s']", parentAttribute, value));
            }
        }
        if (!Strings.isNullOrEmpty(id)) {
            return new Locator("id", id);
        } else if ("a".equals(element.getTagName())) {
            return new Locator("link text", element.getText());
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

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("type", this.type.toString());
        o.put("value", this.value);
        return o;
    }

    @Override
    public String toString() {
        try {
            return this.toJSON().toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String toPrettyString() {
        return this.type.name().toLowerCase() + ":" + this.value;
    }

    public enum Type {
        ID {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElementById(value);
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElementsById(value);
            }
        },
        NAME {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElementByName(value);
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElementsByName(value);
            }
        },
        LINK_TEXT {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElementByLinkText(value);
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElementsByLinkText(value);
            }
        },
        CSS_SELECTOR {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElementByCssSelector(value);
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElementsByCssSelector(value);
            }
        },
        XPATH {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().findElementByXPath(value);
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return ctx.driver().findElementsByXPath(value);
            }
        },
        FOCUS {
            @Override
            public WebElement find(String value, TestRun ctx) {
                return ctx.driver().switchTo().activeElement();
            }

            @Override
            public List<WebElement> findElements(String value, TestRun ctx) {
                return Lists.newArrayList(ctx.driver().switchTo().activeElement());
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
