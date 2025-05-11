package at.ac.tuwien.sepr.groupphase.backend.entity;

import java.security.SecureRandom;
import java.util.Base64;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "salts")
public class Salt {
    public static String generate() {
        return Salt.generate((byte) 16);
    }

    public static String generate(byte len) {
        byte[] salt = new byte[len];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    @Id
    @OneToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    private String salt;

    public void setUser(ApplicationUser user) {
        this.user = user;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
