package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a label entity.
 */
@Entity
@Getter
@Setter
@Table(name = "labels")
public class Label {

    /**
     * Name od this label.
     */
    @Id
    @Column(name = "label_name")
    private String name;

    /**
     * Color of this label.
     */
    private String color;
}