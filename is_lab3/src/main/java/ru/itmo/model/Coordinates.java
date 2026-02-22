package ru.itmo.model;

public class Coordinates {
    private Long id;
    private Float x;
    private Long y;

    public Coordinates() {}

    public Coordinates(Float x, Long y) {
        this.x = x;
        this.y = y;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Float getX() { return x; }
    public void setX(Float x) { this.x = x; }

    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        if (y > 185) {
            throw new IllegalArgumentException("Y can't be greater then 185");
        }
        this.y = y;
    }
}
