package com.lee.smart.data;

import java.io.Serializable;
import java.util.List;

public class LocationEntity implements Serializable, Cloneable {
    public static final int STATE_DISABLE = 0;
    public static final int STATE_ENABLE = 1;
    public static final int STATE_ACTIVE = 2;
    private static final long serialVersionUID = -3102769981513269483L;
    private int id;
    private String name;
    private double longitude;
    private double latitude;
    private int range;
    private int status;
    private List<SettingEntity> settings;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<SettingEntity> getSettings() {
        return settings;
    }

    public void setSettings(List<SettingEntity> settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return "LocationEntity [id=" + id + ", name=" + name + ", longitude=" + longitude + ", latitude=" + latitude
                + ", range=" + range + ", status=" + status + ", settings=" + settings + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
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
        LocationEntity other = (LocationEntity) obj;
        if (id != other.id)
            return false;
        return true;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
