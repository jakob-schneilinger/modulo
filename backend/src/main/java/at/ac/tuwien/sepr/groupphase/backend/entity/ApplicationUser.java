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

@Entity
@Table(name = "users")
public class ApplicationUser {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    private String username;

    @Setter
    private String displayName;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Password password;

    @Getter
    @Setter
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Salt salt;

    private String avatar;
    private ImageType avatarType;

    public ApplicationUser() {
    }

    public ApplicationUser(String name, String email, String password) {
        this.username = name;
        this.email = email;
        this.password = new Password();
    }

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
