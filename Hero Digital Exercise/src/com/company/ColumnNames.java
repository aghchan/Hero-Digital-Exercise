package com.company;

/**
 * Created by Alan on 7/11/2017.
 */
public enum ColumnNames {
    NAME("name", ""),
    HTML_URL("html_url", ""),
    LOGIN("login", "owner"),
    AVATAR_URL("avatar_url", "owner");

    private String name;
    private String nestedEntry;

    ColumnNames(String name, String nestedEntry) {
        this.name = name;
        this.nestedEntry = nestedEntry;
    }

    public String getName() {
        return name;
    }

    public String getNestedEntry() {
        return nestedEntry;
    }
}
