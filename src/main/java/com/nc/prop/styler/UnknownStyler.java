package com.nc.prop.styler;

import java.util.Collection;
import java.util.Collections;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 *
 * @author Neural Cortex
 */
public class UnknownStyler {

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        spansBuilder.add(Collections.emptyList(),text.length());
        return spansBuilder.create();
    }
}
