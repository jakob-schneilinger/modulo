package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.VideoCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.VideoUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.VideoService;


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
 * REST controller for managing video component operations.
 * This controller provides endpoints for creating, updating and retrieving video components.
 */
@RestController
@RequestMapping(value = "/api/v1/component/video")
public class VideoComponentEndpoint {

    private final VideoService service;

    @Autowired
    public VideoComponentEndpoint(VideoService videoService) {
        this.service = videoService;
    }

    /**
     * Gets the video file of the video component with given id.
     *
     * @param id of the component to get
     * @return video file of component
     */
    @GetMapping("{id}")
    public ResponseEntity<FileSystemResource> getVideo(
            @PathVariable(name = "id") long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + id);
        return ResponseEntity.ok().headers(headers).body(service.getVideo(id));
    }

    /**
     * Creates an video component.
     *
     * @param video component data
     * @param file video file data
     * @return component detail of created video
     */
    @PostMapping("")
    public ResponseEntity<ComponentDetailDto> createImageComponent(
            @RequestPart(name = "component", required = true) VideoCreateDto video,
            @RequestPart(name = "video", required = false) MultipartFile file) {
        ComponentDetailDto result;
        try {
            if (file == null || file.isEmpty()) {
                result = service.createVideo(video, null);
            } else {
                result = service.createVideo(video, file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Updates an video component.
     *
     * @param video component data
     * @param file video file data
     * @return component detail of updated video
     */
    @PutMapping("")
    public ResponseEntity<ComponentDetailDto> updateVideoComponent(
            @RequestPart(name = "component", required = true) VideoUpdateDto video,
            @RequestPart(name = "video", required = false) MultipartFile file) {
        ComponentDetailDto result;
        try {
            if (file == null || file.isEmpty()) {
                result = service.updateVideo(video, null);
            } else {
                result = service.updateVideo(video, file.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
