package com.globant.study.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "rule")
public class RuleEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    // Template is the .json file for which this rule is applied
    String template;

    // Property type such as License, Permission, Client
    String propertyName;

    // Value of the property
    String propertyValue;

    // Json id that will be removed if the property and value matches
    String jsonItem;

    // Include will overwrite the default properties and include or exclude tem json component
    Boolean include;

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

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getJsonItem() {
        return jsonItem;
    }

    public void setJsonItem(String jsonItem) {
        this.jsonItem = jsonItem;
    }

    public Boolean getInclude() {
        return include;
    }

    public void setInclude(Boolean include) {
        this.include = include;
    }
}
