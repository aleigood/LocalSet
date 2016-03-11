package com.lee.smart.data;

import java.io.Serializable;

public class SettingEntity implements Serializable, Cloneable {
    private static final long serialVersionUID = 3614089260549641702L;

    private int id;
    private int locationId;
    private int param;
    private String value;

    public SettingEntity() {
    }

    public SettingEntity(int param, String value) {
        super();
        this.param = param;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SettingEntity [id=" + id + ", locationId=" + locationId + ", param=" + param + ", value=" + value + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + param;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SettingEntity other = (SettingEntity) obj;
        if (param != other.param)
            return false;
        return true;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
