package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents a user entity.
 */
@Entity
@Table(name = "users")
public class ApplicationUser {

    /**
     * ID of this user.
     */
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username of this user.
     */
    @Getter
    @Setter
    private String username;

    /**
     * Displayname of this user.
     */
    @Setter
    private String displayName;

    /**
     * Email of this user.
     */
    @Getter
    @Setter
    private String email;

    /**
     * Password of this user.
     */
    @Getter
    @Setter
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Password password;

    /**
     * Salt of this user.
     */
    @Getter
    @Setter
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Salt salt;

    /**
     * Default constructor.
     */
    public ApplicationUser() {
    }

    /**
     * Test constructor.
     *
     * @param name of the user
     * @param email of the user
     * @param password of the user
     */
    public ApplicationUser(String name, String email, String password) {
        this.username = name;
        this.email = email;
        this.password = new Password();
    }

    /**
     * Gets the displayname of this user.
     *
     * @return displayname or username if there is none.
     */
    public String getDisplayName() {
        return displayName != null ? displayName : getUsername();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApplicationUser other) {
            return Objects.equals(other.getId(), this.getId());
        }
        return false;
    }
}