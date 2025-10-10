package com.globant.study.dto;

import java.util.List;

public class ScreenDTO {

    String fieldName;
    String fieldLabel;
    String fieldType;
    String function;
    Boolean includeByDefault = false;
    List<Integer> position;
    List<String> options;
    Boolean include = false;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Boolean getIncludeByDefault() {
        return includeByDefault;
    }

    public void setIncludeByDefault(Boolean includeByDefault) {
        this.includeByDefault = includeByDefault;
    }

    public List<Integer> getPosition() {
        return position;
    }

    public void setPosition(List<Integer> position) {
        this.position = position;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public Boolean getInclude() {
        return include;
    }

    public void setInclude(Boolean include) {
        this.include = include;
    }

    @Override
    public String toString() {
        return "ScreenDTO{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldLabel='" + fieldLabel + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", function='" + function + '\'' +
                ", includeByDefault=" + includeByDefault +
                ", position=" + position +
                ", options=" + options +
                ", include=" + include +
                '}';
    }
}
