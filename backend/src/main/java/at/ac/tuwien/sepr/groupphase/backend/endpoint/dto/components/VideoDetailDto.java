package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for retrieving a video component.
 */
public record VideoDetailDto(long id, Long parentId, long width, long height, long column, long row)
        implements ComponentDetailDto {
}
