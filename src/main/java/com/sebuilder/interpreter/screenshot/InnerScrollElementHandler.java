package com.sebuilder.interpreter.screenshot;

import com.google.common.collect.Maps;

import java.util.TreeMap;

public interface InnerScrollElementHandler {

    InnerScrollElementHandler ignoreInnerScroll = new InnerScrollElementHandler() {
        @Override
        public TreeMap<Integer, InnerElement> handleTarget(Printable parent) {
            return Maps.newTreeMap();
        }
    };

    TreeMap<Integer, InnerElement> handleTarget(Printable parent);

}
