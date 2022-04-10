package com.ashraf.ojapilayer.enums;

public enum ProgrammingLanguage {
    C("c"),
    CPP("c++"),
    JAVA("java");

    private final String value;

    ProgrammingLanguage(String value) {
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}
