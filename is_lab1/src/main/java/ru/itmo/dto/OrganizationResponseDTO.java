package ru.itmo.dto;

import java.time.LocalDateTime;
import ru.itmo.model.OrganizationType;

public class OrganizationResponseDTO {
    private Long id;
    private String name;
    private CoordinatesDTO coordinates;
    private LocalDateTime creationDate;

    private AddressDTO officialAddress;
    private Double annualTurnover;
    private Integer employeesCount;
    private Integer rating;
    private OrganizationType type;
    private AddressDTO postalAddress;

    public OrganizationResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CoordinatesDTO getCoordinates() { return coordinates; }
    public void setCoordinates(CoordinatesDTO coordinates) { this.coordinates = coordinates; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public AddressDTO getOfficialAddress() { return officialAddress; }
    public void setOfficialAddress(AddressDTO officialAddress) { this.officialAddress = officialAddress; }

    public Double getAnnualTurnover() { return annualTurnover; }
    public void setAnnualTurnover(Double annualTurnover) { this.annualTurnover = annualTurnover; }

    public Integer getEmployeesCount() { return employeesCount; }
    public void setEmployeesCount(Integer employeesCount) { this.employeesCount = employeesCount; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public OrganizationType getType() { return type; }
    public void setType(OrganizationType type) { this.type = type; }

    public AddressDTO getPostalAddress() { return postalAddress; }
    public void setPostalAddress(AddressDTO postalAddress) { this.postalAddress = postalAddress; }
}
