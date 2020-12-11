package com.example.sharecaring.model;

import java.io.Serializable;

public class Offer implements Serializable {
    public String description;
    public String address;
    public boolean animals;
    public boolean shopping;
    public boolean medication;
    public boolean transport;

    public Offer(String description, String address, Boolean animals, Boolean shopping, Boolean medication, Boolean transport) {
        this.description = description;
        this.address = address;
        this.animals = animals;
        this.shopping = shopping;
        this.medication = medication;
        this.transport = transport;
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
