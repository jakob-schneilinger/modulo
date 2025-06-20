package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ImageService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import java.io.IOException;

/**
 * REST controller for managing image component operations.
 * This controller provides endpoints for creating, updating and retrieving image components.
 */
@RestController
@RequestMapping(value = "/api/v1/component/image")
public class ImageComponentEndpoint {

    private final ImageService service;

    @Autowired
    public ImageComponentEndpoint(ImageService imageService) {
        this.service = imageService;
    }

    /**
     * Gets the image file of the image component with given id.
     *
     * @param id of the component to get
     * @return image file of component
     */
    @GetMapping("{id}")
    public ResponseEntity<FileSystemResource> getImage(
            @PathVariable(name = "id") long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + id);
        return ResponseEntity.ok().headers(headers).body(service.getImage(id));
    }

    /**
     * Creates an image component.
     *
     * @param image component data
     * @param file image file data
     * @return component detail of created image
     */
    @PostMapping("")
    public ResponseEntity<ComponentDetailDto> createImageComponent(
            @RequestPart(name = "component", required = true) ImageCreateDto image,
            @RequestPart(name = "image", required = false) MultipartFile file) {
        ComponentDetailDto result;
        try {
            if (file == null || file.isEmpty()) {
                result = service.createImage(image, null);
            } else {
                result = service.createImage(image, file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Updates an image component.
     *
     * @param image component data
     * @param file image file data
     * @return component detail of updated image
     */
    @PutMapping("")
    public ResponseEntity<ComponentDetailDto> updateImageComponent(
            @RequestPart(name = "component", required = true) ImageUpdateDto image,
            @RequestPart(name = "image", required = false) MultipartFile file) {
        ComponentDetailDto result;
        try {
            if (file == null || file.isEmpty()) {
                result = service.updateImage(image, null);
            } else {
                result = service.updateImage(image, file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
