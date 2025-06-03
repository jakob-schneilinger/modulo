package at.ac.tuwien.sepr.groupphase.backend.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class ImageComponentValidator {

    private static final long MAX_IMAGE_SIZE_BYTES = 4 * 1024 * 1024;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentValidator componentValidator;

    public ImageComponentValidator(ComponentValidator componentValidator) {
        this.componentValidator = componentValidator;
    }

    /**
     * Validates an image.
     *
     * @param imageDto component to be validated
     * @param imageData to be validated
     */
    public void validateImage(ImageDto imageDto, byte[] imageData, long selfId) {
        LOG.trace("validateImage({}, imageData)", imageDto);

        List<String> errors = new ArrayList<>(componentValidator.validateComponent(imageDto, selfId));

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
        LOG.trace("isValidImageType({})", imageData);

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