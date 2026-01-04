package com.nc.prop.styler;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 *
 * @author Neural Cortex
 */
public class PropertiesStyler {

    private static final String COMMENT_PATTERN = "\\s*#.*";
    private static final String VARIABLES_PATTERN = "([\\w.]+)";
    private static final String STRING_PATTERN = "=\\s*(.*)";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMENT>" + COMMENT_PATTERN + ")"
            + "|(?<VARIABLES>" + VARIABLES_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
    );

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = matcher.group("COMMENT") != null ? "comment"
                    : matcher.group("STRING") != null ? "text"
                    : matcher.group("VARIABLES") != null ? "variables" : null;

            int start = matcher.start();

            spansBuilder.add(Collections.emptyList(), start - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
