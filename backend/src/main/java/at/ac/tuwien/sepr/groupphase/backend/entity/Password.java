package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a password entity.
 */
@Entity
@Getter
@Setter
@Table(name = "passwords")
public class Password {

    /**
     * User associated with this password.
     */
    @Id
    @OneToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    /**
     * Hash of this password.
     */
    private String hash;
}
