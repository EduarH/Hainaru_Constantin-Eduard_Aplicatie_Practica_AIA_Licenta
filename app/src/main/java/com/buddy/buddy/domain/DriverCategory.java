package com.buddy.buddy.domain;

public class DriverCategory {
    private String id;
    private String name;

    public DriverCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return id;
    }
}
