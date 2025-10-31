package com.globant.study.sql.entity;

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

    // Value of the property for which this rule will be applied, example: Licence=free
    String propertyValue;

    // Json id that will be removed if the property and value matches
    String componentName;

    // Include will overwrite the default properties and include or exclude tem json component
    Boolean include;

    // The lower this number, the higher priority on the order, can be null which will place the component on the last priority order
    Integer orderPriority;

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

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Boolean getInclude() {
        return include;
    }

    public void setInclude(Boolean include) {
        this.include = include;
    }

    public Integer getOrderPriority() {
        return orderPriority;
    }

    public void setOrderPriority(Integer orderPriority) {
        this.orderPriority = orderPriority;
    }
}
