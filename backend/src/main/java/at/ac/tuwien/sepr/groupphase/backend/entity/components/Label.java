package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "labels")
public class Label {

    @Id
    @Column(name = "label_name")
    private String name;

    private String color;
}
