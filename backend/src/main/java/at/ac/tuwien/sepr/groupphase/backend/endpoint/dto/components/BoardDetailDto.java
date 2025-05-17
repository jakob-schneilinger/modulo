package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

public record BoardDetailDto(long id, String name, long width, long height, long column, long row, List<ComponentDetailDto> children) implements ComponentDetailDto {
}
