package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BoardDetailDto.class, name = "board"),
    @JsonSubTypes.Type(value = TextDetailDto.class, name = "text"),
})
public interface ComponentDetailDto {

}