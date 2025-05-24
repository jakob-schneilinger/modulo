package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageUpdateDto;
import org.springframework.core.io.FileSystemResource;

public interface ImageService {

    /**
    * Creates an image in the database.
    *
    * @param imageDto component dto for the image
    * @return Component detail of the image created
    */
    ComponentDetailDto createImage(ImageCreateDto imageDto);

    /**
    * Creates an image in the database.
    *
    * @param imageDto component dto for the image
    * @param imageData raw data for the image
    * @return Component detail of the image created
    */
    ComponentDetailDto createImage(ImageCreateDto imageDto, byte[] imageData);

    /**
    * Updates an image in the database.
    *
    * @param imageDto component dto for the image
    * @return Component detail of the image created
    */
    ComponentDetailDto updateImage(ImageUpdateDto imageDto);

    /**
    * Updates an image in the database.
    *
    * @param imageDto component dto for the image
    * @param imageData raw data for the image
    * @return Component detail of the image created
    */
    ComponentDetailDto updateImage(ImageUpdateDto imageDto, byte[] imageData);

    /**
    * Gets image.
    *
    * @param id of the image to get
    * @return file resource
    */
    FileSystemResource getImage(long id);
}
