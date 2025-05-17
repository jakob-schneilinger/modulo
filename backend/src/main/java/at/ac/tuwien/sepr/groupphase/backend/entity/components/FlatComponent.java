package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "components")
public class FlatComponent {

    @Id
    private Long id;

    @Column(name = "component_column")
    private Long column;

    @Column(name = "component_row")
    private Long row;

    private Long width;
    private Long height;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "type")
    private String type;

    public Long getId() {
        return id;
    }

    public Long getColumn() {
        return column;
    }

    public void setColumn(Long column) {
        this.column = column;
    }

    public Long getRow() {
        return row;
    }

    public void setRow(Long row) {
        this.row = row;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
