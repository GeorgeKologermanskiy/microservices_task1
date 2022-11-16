package com.example.homework.domain;

import javax.persistence.*;

@Entity
@Table(name = "storage_table")
public class StorageTable {

    @Id
    @Column(name = "key", length = 100500, unique = true, nullable = false)
    private String key;

    @Column(name = "value", length = 100500, unique = false, nullable = false)
    private String value;

    public StorageTable() {

    }

    public StorageTable(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
