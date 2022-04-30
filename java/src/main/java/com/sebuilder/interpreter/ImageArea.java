package com.sebuilder.interpreter;

import com.github.romankh3.image.comparison.model.Rectangle;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageArea {

    private static Pattern P = Pattern.compile("(?=\\[)\\[((\\d+\\.?\\d*,){3}(\\d+\\.?\\d*))\\]((?=\\[)|$)");

    String value;

    public ImageArea(String value) {
        this.value = value;
    }

    public ImageArea(ImageArea imageArea) {
        this.value = imageArea.value;
    }

    public String getValue() {
        return this.value;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageArea)) return false;
        ImageArea imageArea = (ImageArea) o;
        return Objects.equal(value, imageArea.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "ImageArea{" +
                "value='" + value + '\'' +
                '}';
    }
}
