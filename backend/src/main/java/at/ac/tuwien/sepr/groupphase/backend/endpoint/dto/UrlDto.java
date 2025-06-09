package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record UrlDto(
    @NotBlank(message = "URL must not be blank")
    @Pattern(regexp = "^(https|webcal)://.+$", message = "URL must start with http:// or webcal://")
    String url
) {
}
