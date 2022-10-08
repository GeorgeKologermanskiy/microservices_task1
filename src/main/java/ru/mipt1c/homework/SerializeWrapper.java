package ru.mipt1c.homework;

import java.io.Serializable;

public class SerializeWrapper<K> implements Serializable {

    public SerializeWrapper(K obj) {
        setObj(obj);
    }

    public K getObj() {
        return obj;
    }

    public void setObj(K obj) {
        this.obj = obj;
    }

    private K obj;
}
