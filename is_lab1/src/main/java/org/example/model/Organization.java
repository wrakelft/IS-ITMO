package org.example.model;

import java.time.LocalDateTime;

public class Organization {
    private Long id;
    private String name;
    private Coordinates coordinates;
    private LocalDateTime creationDate;
    private Address officialAddress;
    private Double annualTurnover;
    private Integer employeesCount;
    private Integer rating;
    private OrganizatonType type;
    private Address postalAddress;

    public Organization() {
        this.creationDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID can't be null and lower than 0");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name can't be null or empty");
        }
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        this.coordinates = coordinates;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        if (creationDate == null) {
            throw new IllegalArgumentException("Creation date cannot be null");
        }
        this.creationDate = creationDate;
    }

    public Address getOfficialAddress() {
        return officialAddress;
    }

    public void setOfficialAddress(Address officialAddress) {
        if (officialAddress == null) {
            throw new IllegalArgumentException("Official address cannot be null");
        }
        this.officialAddress = officialAddress;
    }

    public Double getAnnualTurnover() {
        return annualTurnover;
    }

    public void setAnnualTurnover(Double annualTurnover) {
        if (annualTurnover == null || annualTurnover <= 0) {
            throw new IllegalArgumentException("Annual turnover must be greater than 0");
        }
        this.annualTurnover = annualTurnover;
    }

    public Integer getEmployeesCount() {
        return employeesCount;
    }

    public void setEmployeesCount(Integer employeesCount) {
        if (employeesCount == null || employeesCount < 0) {
            throw new IllegalArgumentException("Employees count must be greater than 0");
        }
        this.employeesCount = employeesCount;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        if (rating == null || rating <= 0) {
            throw new IllegalArgumentException("Rating must be greater than 0");
        }
        this.rating = rating;
    }

    public OrganizatonType getType() {
        return type;
    }

    public void setType(OrganizatonType type) {
        this.type = type;
    }

    public Address getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(Address postalAddress) {
        this.postalAddress = postalAddress;
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", officialAddress=" + officialAddress +
                ", annualTurnover=" + annualTurnover +
                ", employeesCount=" + employeesCount +
                ", rating=" + rating +
                ", type=" + type +
                ", postalAddress=" + postalAddress +
                '}';
    }
}
