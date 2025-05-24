package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ImageService;
import jakarta.annotation.security.PermitAll;

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

    @PermitAll // TODO: fix this
    @GetMapping("{id}")
    public ResponseEntity<FileSystemResource> getImage(
            @PathVariable(name = "id") long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + id);
        return ResponseEntity.ok().headers(headers).body(service.getImage(id));
    }

    @PermitAll // TODO: fix this
    @PostMapping("")
    public ResponseEntity<ComponentDetailDto> createImageComponent(
            @RequestPart(name = "component", required = true) ImageCreateDto image,
            @RequestPart(name = "image", required = false) MultipartFile file) {

        ComponentDetailDto result;
        try {
            if (file == null || file.isEmpty()) {
                result = service.createImage(image);
            } else {
                result = service.createImage(image, file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PermitAll // TODO: fix this
    @PutMapping("")
    public ResponseEntity<ComponentDetailDto> setImage(
            @RequestPart(name = "component", required = true) ImageUpdateDto image,
            @RequestPart(name = "image", required = false) MultipartFile file) {
        ComponentDetailDto result;
        try {
            if (file == null || file.isEmpty()) {
                result = service.updateImage(image);
            } else {
                result = service.updateImage(image, file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
