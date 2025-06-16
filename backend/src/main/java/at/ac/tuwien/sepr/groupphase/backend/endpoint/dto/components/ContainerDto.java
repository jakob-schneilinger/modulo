package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * The ContainerDto interface extends ComponentDto, representing
 * Data Transfer Objects (DTOs) for container components.
 */
public interface ContainerDto extends ComponentDto {

    /**
     * Name of this container.
     *
     * @return name
     */
    String name();
}