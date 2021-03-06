package com.sebuilder.interpreter.step.type;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.WebElement;

import java.util.Map;
import java.util.Objects;

public class HighLightElement extends AbstractStepType implements LocatorHolder {

    @Override
    public boolean run(TestRun ctx) {
        Cache.reflesh(ctx);
        boolean result = false;
        for (WebElement element : ctx.locator().findElements(ctx)) {
            Cache.originalStyle(ctx, element);
            toggleHighlight(ctx, element, "2px solid red");
            result = true;
        }
        return result;
    }

    static void toggleHighlight(TestRun ctx, WebElement element, String aStyle) {
        toggleBorder(ctx, element, aStyle);
        if (isChangeOutline(element)) {
            toggleOutline(ctx, element, aStyle);
        }
    }

    private static boolean isChangeOutline(WebElement element) {
        String type = element.getAttribute("type");
        return Objects.equals(type, "checkbox") || Objects.equals(type, "radio");
    }

    private static void toggleOutline(TestRun ctx, WebElement element, String aStyle) {
        ctx.driver().executeScript("arguments[0].style.outline = '" + aStyle + "'; return [];", element);
    }

    private static void toggleBorder(TestRun ctx, WebElement element, String aStyle) {
        ctx.driver().executeScript("arguments[0].style.border = '" + aStyle + "'; return [];", element);
    }

    public static class Cache {
        private static String currentUrl;
        static Map<Locator, BackupStyle> cache = Maps.newHashMap();

        public static void reflesh(TestRun aCtx) {
            String newUrl = aCtx.driver().getCurrentUrl();
            if (Objects.equals(currentUrl, newUrl)) {
                cache.entrySet()
                        .stream()
                        .forEach(entry -> {
                            entry.getKey().
                                    findElements(aCtx)
                                    .stream()
                                    .forEach(element -> {
                                        BackupStyle style = entry.getValue();
                                        toggleBorder(aCtx, element, style.border);
                                        if (isChangeOutline(element)) {
                                            toggleOutline(aCtx, element, style.outline);
                                        }
                                    });
                        });
            }
            currentUrl = newUrl;
            cache.clear();
        }

        public static void originalStyle(TestRun ctx, WebElement element) {
            String originalBorder = (String) ctx.driver().executeScript("return arguments[0].style.border;", element);
            String originalOutline = null;
            if (isChangeOutline(element)) {
                originalOutline = (String) ctx.driver().executeScript("return arguments[0].style.outline;", element);
            }
            Locator locator = Locator.of(ctx.driver(), element);
            cache.put(locator, new BackupStyle(originalBorder, originalOutline));
        }

    }

    private static class BackupStyle {
        private String border;
        private String outline;

        public BackupStyle(String border, String outline) {
            this.border = border;
            this.outline = outline;
        }
    }
}
