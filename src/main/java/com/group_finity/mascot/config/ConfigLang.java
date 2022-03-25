package com.group_finity.mascot.config;

import java.util.Locale;
import java.util.ResourceBundle;

public enum ConfigLang {

    EN(Locale.ENGLISH),
    JP(Locale.JAPANESE);

    private final ResourceBundle resourceBundle;

    ConfigLang(Locale locale) {
        this.resourceBundle = ResourceBundle.getBundle("schema", locale);
    }

    public ResourceBundle getRb() {
        return resourceBundle;
    }

    public String tr(String s) {
        return getRb().getString(s);
    }

}
