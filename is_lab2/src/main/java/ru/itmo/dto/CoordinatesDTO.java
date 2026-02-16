package ru.itmo.dto;

public class CoordinatesDTO {
    private Long id;
    private Float x;
    private Long y;

    public CoordinatesDTO() {}

    public CoordinatesDTO(Float x, Long y) {
        this.x = x;
        this.y = y;
    }

    public CoordinatesDTO(Long id, Float x, Long y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Float getX() { return x; }
    public void setX(Float x) { this.x = x; }

    public Long getY() { return y; }
    public void setY(Long y) { this.y = y; }
}
