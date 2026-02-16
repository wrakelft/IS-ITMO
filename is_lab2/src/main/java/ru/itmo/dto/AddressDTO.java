package ru.itmo.dto;

public class AddressDTO {
    private Long id;
    private String street;

    public AddressDTO() {}

    public AddressDTO(String street) {
        this.street = street;
    }

    public AddressDTO(Long id, String street) {
        this.id = id;
        this.street = street;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
}
