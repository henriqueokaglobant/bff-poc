package com.globant.study.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.globant.study.sql.entity.ComponentEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentDTO {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String template;
    String name;
    @JsonIgnore
    String titleKey;
    String title; // Calculate field
    String componentType;
    @JsonIgnore
    Boolean include = false; // Calculate field
    @JsonIgnore
    Boolean excludeByDefault = false;
    Integer orderPriority;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, Object> properties = new HashMap<>();

    @JsonIgnore
    ComponentDTO optionParentComponent;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ComponentDTO> options = new ArrayList<>();

    @JsonIgnore
    ComponentEntity parentComponent;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
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

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "ComponentDTO{" +
                "label='" + title + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
