package com.globant.study.dto;

import java.util.List;

public class ScreenComponentDTO {

    String template;
    String name;
    String labelKey;
    String type;
    String function;
    String style;
    Boolean includeByDefault = false;
    Integer orderPriority;
    List<String> options;

    // Calculated fields
    String label;
    Boolean include = false;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Boolean getIncludeByDefault() {
        return includeByDefault;
    }

    public void setIncludeByDefault(Boolean includeByDefault) {
        this.includeByDefault = includeByDefault;
    }

    public Integer getOrderPriority() {
        return orderPriority;
    }

    public void setOrderPriority(Integer orderPriority) {
        this.orderPriority = orderPriority;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getInclude() {
        return include;
    }

    public void setInclude(Boolean include) {
        this.include = include;
    }

    @Override
    public String toString() {
        return "ScreenComponentDTO{" +
                "templateName='" + template + '\'' +
                ", fieldName='" + name + '\'' +
                ", fieldLabel='" + labelKey + '\'' +
                ", fieldType='" + type + '\'' +
                ", function='" + function + '\'' +
                ", style='" + style + '\'' +
                ", includeByDefault=" + includeByDefault +
                ", orderPriority=" + orderPriority +
                ", options=" + options +
                ", label='" + label + '\'' +
                ", include=" + include +
                '}';
    }
}
