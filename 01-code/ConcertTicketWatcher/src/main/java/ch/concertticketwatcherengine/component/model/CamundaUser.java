package ch.concertticketwatcherengine.component.model;

import ch.concertticketwatcherengine.core.generic.Model;

public class CamundaUser implements Model {
    private String username;
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}