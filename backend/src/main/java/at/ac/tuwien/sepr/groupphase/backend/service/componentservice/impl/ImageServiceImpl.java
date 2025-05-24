package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Image;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${global.location.image}")
    private String imagePath;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final ComponentValidator componentValidator;
    private final ComponentService componentService;

    @Autowired
    public ImageServiceImpl(ComponentRepository componentRepository, ComponentValidator componentValidator,
            ComponentService componentService) {
        this.componentRepository = componentRepository;
        this.componentValidator = componentValidator;
        this.componentService = componentService;
    }

    @Override
    public ComponentDetailDto createImage(ImageCreateDto imageDto) {
        LOG.trace("Creating image {}...", imageDto);
        long userId = componentService.getUserId();

        List<String> errors = new ArrayList<>(componentValidator.validateComponent(imageDto, userId, -1L));

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation of image failed", errors);
        }

        return componentService.setComponent(imageDto, new Image(), userId);
    }

    @Override
    public ComponentDetailDto createImage(ImageCreateDto imageDto, byte[] imageData) {
        ComponentDetailDto result = createImage(imageDto);

        if (!isValidImageType(imageData)) {
            throw new ValidationException("Validation failed for image creation", List.of("Not correct image type (jpg, png, gif)"));
        }

        saveImageData(result.id(), imageData);

        return result;
    }

    @Override
    @Transactional
    public ComponentDetailDto updateImage(ImageUpdateDto imageDto) {
        LOG.trace("Updating image {}...", imageDto);

        Optional<Component> optionalComponent = componentRepository.findById(imageDto.id());
        Component component;

        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
        } else {
            throw new NotFoundException("Image with given ID does not exist");
        }

        long userId = componentService.getUserId();

        if (!component.getOwnerId().equals(userId)) {
            throw new UserNotAuthorizedException("User is not owner of this component");
        }

        List<String> errors = new ArrayList<>(
                componentValidator.validateComponent(imageDto, userId, component.getId()));

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation of image failed", errors);
        }

        return componentService.setComponent(imageDto, component, userId);
    }

    @Override
    @Transactional
    public ComponentDetailDto updateImage(ImageUpdateDto imageDto, byte[] imageData) {
        ComponentDetailDto result = this.updateImage(imageDto);

        if (!isValidImageType(imageData)) {
            throw new ValidationException("Validation failed for image creation", List.of("Not correct image type (jpg, png, gif)"));
        }

        saveImageData(result.id(), imageData);

        return result;
    }

    @Override
    public FileSystemResource getImage(long id) {
        LOG.trace("Getting image {}...", id);

        File image = new File(imagePath + id);

        if (!image.exists()) {
            throw new NotFoundException("Image with given ID does not exist");
        }

        return new FileSystemResource(image);
    }

    private void saveImageData(long filename, byte[] imageData) {
        File dir = new File(imagePath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Unable to create directory " + imagePath);
            }
        }

        try {
            File file = new File(imagePath, String.valueOf(filename));
            var stream = new FileOutputStream(file);
            stream.write(imageData);
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't store file. This should not happen. Please contact an administrator.");
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
