package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.util.List;

public record BoardDetailDto(long id, String name, long width, long height, long column, long row, List<ComponentDetailDto> children) implements ComponentDetailDto {
}
