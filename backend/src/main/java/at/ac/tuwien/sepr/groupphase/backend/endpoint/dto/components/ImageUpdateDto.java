package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageUpdateDto(@JsonProperty("id") long id, Long parentId, long width, long height, long column, long row)
        implements ImageDto {
}
