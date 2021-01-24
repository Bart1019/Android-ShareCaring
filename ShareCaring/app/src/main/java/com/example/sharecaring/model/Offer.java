package com.example.sharecaring.model;

import java.io.Serializable;

public class Offer implements Serializable {
    public String description;
    public String address;
    public boolean animals;
    public boolean shopping;
    public boolean medication;
    public boolean transport;
    public boolean isAccepted;
    public boolean isVolunteering;

    public Offer(String description, String address, Boolean animals, Boolean shopping, Boolean medication, Boolean transport, Boolean isAccepted, Boolean isVolunteering) {
        this.description = description;
        this.address = address;
        this.animals = animals;
        this.shopping = shopping;
        this.medication = medication;
        this.transport = transport;
        this.isAccepted = isAccepted;
        this.isVolunteering = isVolunteering;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public boolean isAnimals() {
        return animals;
    }

    public boolean isShopping() {
        return shopping;
    }

    public boolean isMedication() {
        return medication;
    }

    public boolean isTransport() {
        return transport;
    }
}
