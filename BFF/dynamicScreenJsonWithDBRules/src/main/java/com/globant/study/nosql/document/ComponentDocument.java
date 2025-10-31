package com.globant.study.nosql.document;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "component")
public class ComponentDocument {

    @Id
    String id;
    String template;
    String name;
    String titleKey;
    String componentType;
    Boolean excludeByDefault = false;
    List<ComponentDocument> children = new ArrayList<>();
    List<ComponentDocument> options = new ArrayList<>();
    private Map<String, Object> properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public Boolean getExcludeByDefault() {
        return excludeByDefault;
    }

    public void setExcludeByDefault(Boolean excludeByDefault) {
        this.excludeByDefault = excludeByDefault;
    }

    public List<ComponentDocument> getChildren() {
        return children;
    }

    public void setChildren(List<ComponentDocument> children) {
        this.children = children;
    }

    public List<ComponentDocument> getOptions() {
        return options;
    }

    public void setOptions(List<ComponentDocument> options) {
        this.options = options;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
