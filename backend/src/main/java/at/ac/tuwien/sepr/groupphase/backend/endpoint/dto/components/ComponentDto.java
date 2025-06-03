package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * The ComponentDto interface serves as a blueprint for Data Transfer Objects (DTOs)
 * representing components in a system. It defines the essential properties that
 * all component DTOs must implement, ensuring consistency across different component types.
 */
public interface ComponentDto {

    /**
     * ID of the parent component of this component.
     *
     * @return id
     */
    Long parentId();

    /**
     * Width of this component.
     *
     * @return width
     */
    Long width();

    /**
     * Height of this component.
     *
     * @return height
     */
    Long height();

    /**
     * Column position of this component.
     *
     * @return column
     */
    Long column();

    /**
     * Row position of this component.
     *
     * @return row
     */
    Long row();
}