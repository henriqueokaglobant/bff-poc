package com.globant.study.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
public class ComponentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String template;
    String name;
    String titleKey;
    String componentType;
    Boolean excludeByDefault = false;
    List<Integer> position;

    @OneToMany(mappedBy = "parentComponent", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ComponentEntity> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER) // Should always come populated
    @JoinColumn(name = "parent_component_id")
    ComponentEntity parentComponent;

    @OneToMany(mappedBy = "optionParentComponent", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ComponentEntity> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER) // Should always come populated
    @JoinColumn(name = "option_parent_component_id")
    ComponentEntity optionParentComponent;

    @JdbcTypeCode(SqlTypes.JSON) // Requires Hibernate 6+ and a suitable database dialect
    @Column(name = "properties")
    private Map<String, Object> properties;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public List<Integer> getPosition() {
        return position;
    }

    public void setPosition(List<Integer> position) {
        this.position = position;
    }

    public List<ComponentEntity> getChildren() {
        return children;
    }

    public void setChildren(List<ComponentEntity> children) {
        this.children = children;
    }

    public ComponentEntity getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(ComponentEntity parentComponent) {
        this.parentComponent = parentComponent;
    }

    public List<ComponentEntity> getOptions() {
        return options;
    }

    public void setOptions(List<ComponentEntity> options) {
        this.options = options;
    }

    public ComponentEntity getOptionParentComponent() {
        return optionParentComponent;
    }

    public void setOptionParentComponent(ComponentEntity optionParentComponent) {
        this.optionParentComponent = optionParentComponent;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
