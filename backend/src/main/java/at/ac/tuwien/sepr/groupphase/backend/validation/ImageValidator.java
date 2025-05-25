package at.ac.tuwien.sepr.groupphase.backend.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class ImageValidator {

    private static final long MAX_IMAGE_SIZE_BYTES = 4 * 1024 * 1024;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentValidator componentValidator;

    public ImageValidator(ComponentValidator componentValidator) {
        this.componentValidator = componentValidator;
    }

    /**
     * Validates an image for creation.
     *
     * @param imageDto component to be validated
     * @param imageData to be validated
     * @param userId of the user that wants to create the image
     */
    public void validateImageCreation(ImageCreateDto imageDto, byte[] imageData, long userId) {
        LOG.trace("validateImageCreation({}, imageData)", imageDto);

        List<String> errors = new ArrayList<>();
        errors.addAll(componentValidator.validateComponent(imageDto, userId, -1L));

        if (imageData != null) {
            if (imageData.length > MAX_IMAGE_SIZE_BYTES) {
                errors.add("Image exceeds the maximum size of 4MB");
            } else if (!isValidImageType(imageData)) {
                errors.add("Not correct image type (jpg, png, gif)");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation for updating board failed", errors);
        }
    }

    /**
     * Validates an image for update.
     *
     * @param imageDto component to be validated
     * @param imageData to be validated
     * @param userId of the user that wants to update the image
     */
    public void validateImageUpdate(ImageUpdateDto imageDto, Component imageEntity, byte[] imageData, long userId) {
        LOG.trace("validateImageUpdate({}, {}, imageData)", imageDto, imageEntity);

        if (!imageEntity.getOwnerId().equals(userId)) {
            throw new UserNotAuthorizedException("User is not owner of this component");
        }

        List<String> errors = new ArrayList<>();
        errors.addAll(componentValidator.validateComponent(imageDto, userId, imageEntity.getId()));

        if (imageData != null) {
            if (imageData.length > MAX_IMAGE_SIZE_BYTES) {
                errors.add("Image exceeds the maximum size of 4MB");
            } else if (!isValidImageType(imageData)) {
                errors.add("Not correct image type (jpg, png, gif)");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation for updating board failed", errors);
        }
    }

    private boolean isValidImageType(byte[] imageData) {
        if (imageData == null || imageData.length < 3) {
            return false;
        }

        // JPEG: FF D8 FF
        if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8 && imageData[2] == (byte) 0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (imageData.length >= 4 && imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50
            && imageData[2] == (byte) 0x4E && imageData[3] == (byte) 0x47) {
            return true;
        }

        // GIF: 47 49 46 38 37|39 61 ("GIF87a" or "GIF89a")
        if (imageData.length >= 6 && imageData[0] == (byte) 0x47 && imageData[1] == (byte) 0x49
            && imageData[2] == (byte) 0x46 && imageData[3] == (byte) 0x38
            && (imageData[4] == (byte) 0x39 || imageData[4] == (byte) 0x37)
            && imageData[5] == (byte) 0x61) {
            return true;
        }

        return false;
    }
}