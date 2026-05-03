package ch.concertticketwatcherengine.component.model;

import ch.concertticketwatcherengine.core.generic.Model;

public class Location implements Model {
    private double latitude;
    private double longitude;
    private String city;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}