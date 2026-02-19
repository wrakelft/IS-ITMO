package ru.itmo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import ru.itmo.model.OrganizationType;

public class OrganizationRequestDTO {
    private String name;

    private Long version;
    private CoordinatesDTO coordinates;
    private AddressDTO officialAddress;
    private AddressDTO postalAddress;

    private Long coordinatesId;
    private Long officialAddressId;
    private Long postalAddressId;

    @JsonIgnore private boolean postalAddressProvided;
    @JsonIgnore private boolean postalAddressIdProvided;

    private Double annualTurnover;
    private Integer employeesCount;
    private Integer rating;
    private OrganizationType type;

    public OrganizationRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public CoordinatesDTO getCoordinates() { return coordinates; }
    public void setCoordinates(CoordinatesDTO coordinates) { this.coordinates = coordinates; }

    public AddressDTO getOfficialAddress() { return officialAddress; }
    public void setOfficialAddress(AddressDTO officialAddress) { this.officialAddress = officialAddress; }

    public AddressDTO getPostalAddress() { return postalAddress; }
    @JsonSetter(value = "postalAddress", nulls = Nulls.SET)
    public void setPostalAddress(AddressDTO postalAddress) {
        this.postalAddress = postalAddress;
        this.postalAddressProvided = true;
    }

    public Long getCoordinatesId() { return coordinatesId; }
    public void setCoordinatesId(Long coordinatesId) { this.coordinatesId = coordinatesId; }

    public Long getOfficialAddressId() { return officialAddressId; }
    public void setOfficialAddressId(Long officialAddressId) { this.officialAddressId = officialAddressId; }

    public Long getPostalAddressId() { return postalAddressId; }

    @JsonSetter (value = "postalAddressId", nulls = Nulls.SET)
    public void setPostalAddressId(Long postalAddressId) {
        this.postalAddressId = postalAddressId;
        this.postalAddressIdProvided = true;
    }

    @JsonIgnore public boolean isPostalAddressProvided() { return postalAddressProvided; }
    @JsonIgnore public boolean isPostalAddressIdProvided() { return postalAddressIdProvided; }

    public Double getAnnualTurnover() { return annualTurnover; }
    public void setAnnualTurnover(Double annualTurnover) { this.annualTurnover = annualTurnover; }

    public Integer getEmployeesCount() { return employeesCount; }
    public void setEmployeesCount(Integer employeesCount) { this.employeesCount = employeesCount; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public OrganizationType getType() { return type; }
    public void setType(OrganizationType type) { this.type = type; }
}
