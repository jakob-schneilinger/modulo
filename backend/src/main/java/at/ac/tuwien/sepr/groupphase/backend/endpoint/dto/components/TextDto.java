package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Base projection for Text‑components.
 * Mirrors the additional information a Text carries beyond the generic Component data.
 */
public interface TextDto extends ComponentDto {

    /**
     * The actual text.
     *
     * @return text
     */
    String content();
}