package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Image;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ImageService;
import at.ac.tuwien.sepr.groupphase.backend.validation.ImageValidator;
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
import java.util.Optional;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${global.location.image}")
    private String imagePath;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final ComponentService componentService;
    private final ImageValidator imageValidator;

    @Autowired
    public ImageServiceImpl(ComponentRepository componentRepository, ImageValidator imageValidator,
                            ComponentService componentService) {
        this.componentRepository = componentRepository;
        this.imageValidator = imageValidator;
        this.componentService = componentService;
    }

    @Override
    public ComponentDetailDto createImage(ImageCreateDto imageDto, byte[] imageData) {
        LOG.trace("Creating image {}...", imageDto);
        long userId = componentService.getUserId();

        imageValidator.validateImageCreation(imageDto, imageData, userId);

        ComponentDetailDto result = componentService.setComponent(imageDto, new Image(), userId);

        if (imageData != null) {
            saveImageData(result.id(), imageData);
        }

        return result;
    }

    @Override
    @Transactional
    public ComponentDetailDto updateImage(ImageUpdateDto imageDto, byte[] imageData) {
        LOG.trace("Updating image {}...", imageDto);

        Optional<Component> optionalComponent = componentRepository.findById(imageDto.id());
        Component component;

        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
        } else {
            throw new NotFoundException("Image with given ID does not exist");
        }

        long userId = componentService.getUserId();
        imageValidator.validateImageUpdate(imageDto, component, imageData, userId);

        ComponentDetailDto result = componentService.setComponent(imageDto, component, userId);

        if (imageData != null) {
            saveImageData(result.id(), imageData);
        }

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
}
