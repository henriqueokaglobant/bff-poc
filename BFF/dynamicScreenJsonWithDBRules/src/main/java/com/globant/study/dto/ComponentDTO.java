package com.globant.study.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.globant.study.entity.ComponentEntity;

import java.util.ArrayList;
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

    @JsonIgnore
    ComponentDTO optionParentComponent;
    List<ComponentDTO> options = new ArrayList<>();

    @JsonIgnore
    ComponentEntity parentComponent;
    List<ComponentDTO> children = new ArrayList<>();

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public Boolean getInclude() {
        return include;
    }

    public void setInclude(Boolean include) {
        this.include = include;
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

    public ComponentDTO getOptionParentComponent() {
        return optionParentComponent;
    }

    public void setOptionParentComponent(ComponentDTO optionParentComponent) {
        this.optionParentComponent = optionParentComponent;
    }

    public List<ComponentDTO> getOptions() {
        return options;
    }

    public void setOptions(List<ComponentDTO> options) {
        this.options = options;
    }

    public ComponentEntity getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(ComponentEntity parentComponent) {
        this.parentComponent = parentComponent;
    }

    public List<ComponentDTO> getChildren() {
        return children;
    }

    public void setChildren(List<ComponentDTO> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "ComponentDTO{" +
                "label='" + label + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
