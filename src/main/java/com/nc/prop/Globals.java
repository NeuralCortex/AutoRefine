package com.nc.prop;

import java.util.Locale;

public class Globals {

    public static final Locale DEFAULT_LOCALE = Locale.US;

    public static final double HEIGHT = 700;
    public static final double WIDTH = HEIGHT * 18.0f / 9.0f;

    public static final String BUNDLE_PATH = "com.nc.prop.bundle.prop";
    public static final String LOG4J2_CONFIG_PATH = System.getProperty("user.dir") + "/config/log4j2.xml";
    public static final String XML_CONFIG_PATH = System.getProperty("user.dir") + "/config/config.xml";

    public static XMLPropertyManager propman;

    static {
        propman = new XMLPropertyManager(XML_CONFIG_PATH);
    }

    public static final String APP_LOGO_PATH = "/images/kdf.png";
    public static final String CSS_PATH = "/com/nc/prop/style/nc.css";
    public static final String CSS_JAVA_PATH = "/com/nc/prop/style/java.css";

    public static final String FXML_PATH = "/com/nc/prop/fxml/";
    public static final String FXML_TABS_PATH = "/com/nc/prop/fxml/tabs/";

    public static final String FXML_MAIN_PATH = FXML_PATH + "main_app.fxml";

    public static final String FXML_CLEANER_PATH = FXML_TABS_PATH + "cleaner.fxml";
    public static final String FXML_PROPERTIES_PATH = FXML_TABS_PATH + "properties.fxml";
    public static final String FXML_FIELDS_PATH = FXML_TABS_PATH + "fields.fxml";

    public static final String DLG_PROGRESS_PATH = FXML_PATH + "progress.fxml";

    public static final String PATH_PROP_DIR = "PATH_PROP_DIR";
    public static final String PATH_FIELD_DIR = "PATH_FIELD_DIR";

    public static final String UNUSED_FLAG_PROPS = "# [UNUSED] ";
    public static final String UNUSED_FLAG_FIELDS = "// [UNUSED] ";

    public static enum TAB {
        PROPERTIES, FIELDS
    }
}
