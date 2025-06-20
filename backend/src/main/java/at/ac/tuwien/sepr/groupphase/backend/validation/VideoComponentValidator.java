package at.ac.tuwien.sepr.groupphase.backend.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.VideoDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class VideoComponentValidator {

    private static final long MAX_VIDEO_SIZE_BYTES = 20 * 1024 * 1024;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentValidator componentValidator;

    public VideoComponentValidator(ComponentValidator componentValidator) {
        this.componentValidator = componentValidator;
    }

    /**
     * Validates a video.
     *
     * @param videoDto component to be validated
     * @param videoData to be validated
     */
    public void validateVideo(VideoDto videoDto, byte[] videoData, long selfId) {
        LOG.trace("validateImage({}, videoData)", videoDto);

        List<String> errors = new ArrayList<>(componentValidator.validateComponent(videoDto, selfId));

        if (videoData != null) {
            if (videoData.length > MAX_VIDEO_SIZE_BYTES) {
                errors.add("Video exceeds the maximum size of 20MB");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation for updating board failed", errors);
        }
    }
}