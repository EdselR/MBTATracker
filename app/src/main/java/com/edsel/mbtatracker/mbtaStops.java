package com.edsel.mbtatracker;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity
public class mbtaStops implements Serializable{

    @PrimaryKey @NonNull
    public String id;

    @ColumnInfo
    public String name;

    @ColumnInfo
    public String description;

    @ColumnInfo
    public String platform;

    @ColumnInfo
    public String parentID;

    @ColumnInfo
    public double lat;

    @ColumnInfo
    public double lon;

    @ColumnInfo
    public String etaTime = "Not Available";

    @ColumnInfo
    public String nextTime = "Not Available";

    @ColumnInfo
    public String colour = "#00843D";

    @ColumnInfo
    public String routeName;

    public mbtaStops(String id, String name, String description, String platform, String parentID,
                     double lat, double lon){

        this.id = id;
        this.name = name;
        this.description = description;
        this.platform = platform;
        this.parentID = parentID;
        this.lat = lat;
        this.lon = lon;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getParentID() {
        return parentID;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLat() {
        return lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLon() {
        return lon;
    }

    public void setEtaTime(String etaTime) {
        this.etaTime = etaTime;
    }

    public String getEtaTime() {
        return etaTime;
    }

    public void setNextTime(String nextTime) {
        this.nextTime = nextTime;
    }

    public String getNextTime() {
        return nextTime;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getColour() {
        return colour;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteName() {
        return routeName;
    }
}
