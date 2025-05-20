package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Base projection for Text‑components.
 * Mirrors the additional information a Text carries beyond the generic Component data.
 */
public interface TextDto extends ComponentDto {

    String text();        // the actual text content

    long width();

    int fontSize();

    /**
     * The parent container (board / task / …) the Text is added to.
     */
    Long parentId();



}
