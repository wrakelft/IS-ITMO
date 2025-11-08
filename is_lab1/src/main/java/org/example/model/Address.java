package org.example.model;

public class Address {
    private Long id;
    private String street;

    public Address() {}

    public Address(String street) {
        this.street = street;
    }

    public String getStreet() { return street; }
    public void setStreet(String street) {
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Street can't be null or empty");
        }
        this.street = street;
    }
}
