package com.sebuilder.interpreter;

import com.github.romankh3.image.comparison.model.Rectangle;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ImageArea(String value) {

    private static final Pattern P = Pattern.compile("(?=\\[)\\[((\\d+\\.?\\d*,){3}(\\d+\\.?\\d*))\\]((?=\\[)|$)");

    public List<Rectangle> getRectangles() {
        List<Rectangle> result = Lists.newArrayList();
        Matcher m = P.matcher(this.value);
        while (m.find()) {
            String[] points = m.group(1).split(",");
            result.add(new Rectangle(new BigDecimal(points[0]).intValue()
                    , new BigDecimal(points[1]).intValue()
                    , new BigDecimal(points[2]).intValue()
                    , new BigDecimal(points[3]).intValue()
            ));
        }
        return result;
    }

}
