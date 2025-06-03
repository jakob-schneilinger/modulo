package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for setting a label.
 *
 * @param name of the label
 * @param color of the label
 */
public record LabelDto(String name, String color) {
}