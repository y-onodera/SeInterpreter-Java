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
    public boolean run(final TestRun ctx) {
        Cache.refresh(ctx);
        boolean result = false;
        for (final WebElement element : ctx.locator().findElements(ctx)) {
            Cache.originalStyle(ctx, element);
            toggleHighlight(ctx, element);
            result = true;
        }
        return result;
    }

    static void toggleHighlight(final TestRun ctx, final WebElement element) {
        toggleBorder(ctx, element, "2px solid red");
        if (isChangeOutline(element)) {
            toggleOutline(ctx, element, "2px solid red");
        }
    }

    private static boolean isChangeOutline(final WebElement element) {
        final String type = element.getDomAttribute("type");
        return Objects.equals(type, "checkbox") || Objects.equals(type, "radio");
    }

    private static void toggleOutline(final TestRun ctx, final WebElement element, final String aStyle) {
        ctx.executeScript("arguments[0].style.outline = '" + aStyle + "'; return [];", element);
    }

    private static void toggleBorder(final TestRun ctx, final WebElement element, final String aStyle) {
        ctx.executeScript("arguments[0].style.border = '" + aStyle + "'; return [];", element);
    }

    public static class Cache {
        private static String currentUrl;
        static Map<Locator, BackupStyle> cache = Maps.newHashMap();

        public static void refresh(final TestRun aCtx) {
            final String newUrl = aCtx.driver().getCurrentUrl();
            if (Objects.equals(currentUrl, newUrl)) {
                cache.forEach((key, style) -> key.
                        findElements(aCtx)
                        .forEach(element -> {
                            toggleBorder(aCtx, element, style.border);
                            if (isChangeOutline(element)) {
                                toggleOutline(aCtx, element, style.outline);
                            }
                        }));
            }
            currentUrl = newUrl;
            cache.clear();
        }

        public static void originalStyle(final TestRun ctx, final WebElement element) {
            final String originalBorder = (String) ctx.executeScript("return arguments[0].style.border;", element);
            String originalOutline = null;
            if (isChangeOutline(element)) {
                originalOutline = (String) ctx.executeScript("return arguments[0].style.outline;", element);
            }
            final Locator locator = ctx.detectLocator(element);
            cache.put(locator, new BackupStyle(originalBorder, originalOutline));
        }

    }

    private record BackupStyle(String border, String outline) {
    }
}
