package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * The BoardDto interface extends ContainerDto, representing
 * Data Transfer Objects (DTOs) for board components.
 */
public interface BoardDto extends ContainerDto {

    /**
     * Maximum depth for root component.
     *
     * @return depth
     */
    Integer depth();
}