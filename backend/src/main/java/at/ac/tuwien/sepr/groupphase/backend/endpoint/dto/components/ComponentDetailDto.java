package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The ComponentDetailDto interface serves as a blueprint for Data Transfer Objects (DTOs)
 * returning component details. It defines the essential properties that
 * all component detail DTOs must implement, ensuring consistency across different component types.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BoardDetailDto.class, name = "board"),
    @JsonSubTypes.Type(value = TextDetailDto.class, name = "text"),
    @JsonSubTypes.Type(value = TaskDetailDto.class, name = "task"),
    @JsonSubTypes.Type(value = ImageDetailDto.class, name = "image"),
    @JsonSubTypes.Type(value = VideoDetailDto.class, name = "video"),
    @JsonSubTypes.Type(value = NoteDetailDto.class, name = "note"),
    @JsonSubTypes.Type(value = CalendarDetailDto.class, name = "calendar")
})
public interface ComponentDetailDto {

    /**
     * The id of the component.
     *
     * @return id
     */
    long id();

    /**
     * Parent id of the component.
     *
     * @return parentId
     */
    Long parentId();

    /**
     * Height of the component.
     *
     * @return height
     */
    long height();

    /**
     * Width of the component.
     *
     * @return width
     */
    long width();

    /**
     * Row position of the component.
     *
     * @return row
     */
    long row();

    /**
     * Column position of the component.
     *
     * @return column
     */
    long column();
}