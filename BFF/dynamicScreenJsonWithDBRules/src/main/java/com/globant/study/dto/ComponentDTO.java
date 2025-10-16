package com.globant.study.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.globant.study.utils.Utils;

import java.util.List;

public class ComponentDTO {

    String template;
    String name;
    String labelKey;
    String label; // Calculate field
    String type;
    String function;
    String style;
    Boolean include = false; // Calculate field
    Boolean excludeByDefault = false;
    Integer orderPriority;
    List<ComponentDTO> options;

    @JsonIgnore
    ComponentDTO parentComponent;


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

    public Boolean getExcludeByDefault() {
        return excludeByDefault;
    }

    public void setExcludeByDefault(Boolean excludeByDefault) {
        this.excludeByDefault = excludeByDefault;
    }

    public Integer getOrderPriority() {
        return orderPriority;
    }

    public void setOrderPriority(Integer orderPriority) {
        this.orderPriority = orderPriority;
    }

    public List<ComponentDTO> getOptions() {
        return options;
    }

    public void setOptions(List<ComponentDTO> options) {
        this.options = options;
    }

    public ComponentDTO getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(ComponentDTO parentComponent) {
        this.parentComponent = parentComponent;
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
        String componentString = "ComponentDTO{" +
                "template='" + template + '\'' +
                ", name='" + name + '\'' +
                ", labelKey='" + labelKey + '\'' +
                ", label='" + label + '\'' +
                ", type='" + type + '\'' +
                ", function='" + function + '\'' +
                ", style='" + style + '\'' +
                ", include=" + include +
                ", excludeByDefault=" + excludeByDefault +
                ", orderPriority=" + orderPriority +
                ", options=" + options +
                '}';

        if (this.getInclude() && (this.getParentComponent() == null || this.getParentComponent().include)) {
            return Utils.greenNoReset(componentString);
        } else {
            return Utils.redNoReset(componentString);
        }
    }
}
