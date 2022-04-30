package com.sebuilder.interpreter;

import com.github.romankh3.image.comparison.model.Rectangle;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImageAreaTest {

    @Test
    public void getRectanglesIncludeCommaSeparate4decimal() {
        ImageArea target = new ImageArea("[1.0,2.0][2,3,4.0,5][3.4]");
        List<Rectangle> actual = target.getRectangles();
        assertEquals(1, actual.size());
        assertEquals(new Rectangle(2, 3, 4, 5), actual.get(0));
    }

    @Test
    public void getRectanglesNoRectangle() {
        ImageArea target = new ImageArea("[1.0,2.0][2,3,4.0,][3.4]");
        List<Rectangle> actual = target.getRectangles();
        assertEquals(0, actual.size());
    }

    @Test
    public void getRectanglesMultiRectangle() {
        ImageArea target = new ImageArea("[1.0,2.0,3,4][2,3,4.0,6][3,4,4,5]");
        List<Rectangle> actual = target.getRectangles();
        assertEquals(3, actual.size());
        assertEquals(new Rectangle(1, 2, 3, 4), actual.get(0));
        assertEquals(new Rectangle(2, 3, 4, 6), actual.get(1));
        assertEquals(new Rectangle(3, 4, 4, 5), actual.get(2));
    }
}