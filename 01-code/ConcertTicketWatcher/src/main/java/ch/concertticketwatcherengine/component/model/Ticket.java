package ch.concertticketwatcherengine.component.model;

import ch.concertticketwatcherengine.core.generic.Model;

public class Ticket implements Model {
    private boolean available;
    private String  url;
    private String  price;
    private String  presaleDate;

    public String getPresaleDate() {
        return presaleDate;
    }

    public void setPresaleDate(String presaleDate) {
        this.presaleDate = presaleDate;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}