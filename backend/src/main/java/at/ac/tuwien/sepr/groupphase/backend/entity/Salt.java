package at.ac.tuwien.sepr.groupphase.backend.entity;

import java.security.SecureRandom;
import java.util.Base64;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents s salt entity.
 */
@Setter
@Entity
@Table(name = "salts")
public class Salt {

    /**
     * Generate method for the salt.
     *
     * @return generated salt string with length 16
     */
    public static String generate() {
        return Salt.generate((byte) 16);
    }

    /**
     * Generate method for the salt with corresponding length.
     *
     * @param len length of the salt
     * @return generated salt string with corresponding length
     */
    public static String generate(byte len) {
        byte[] salt = new byte[len];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * User associated with this salt.
     */
    @Id
    @OneToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    /**
     * String value of this salt.
     */
    @Getter
    private String salt;
}