package com.globant.study.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "locale")
public class LocalizationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String locale;
    String messageKey;
    String messageValue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageValue() {
        return messageValue;
    }

    public void setMessageValue(String messageValue) {
        this.messageValue = messageValue;
    }

    @Override
    public String toString() {
        return "LocalizationEntity{" +
                "id=" + id +
                ", locale='" + locale + '\'' +
                ", messageKey='" + messageKey + '\'' +
                ", messageValue='" + messageValue + '\'' +
                '}';
    }
}
