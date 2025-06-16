package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.VideoCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.VideoUpdateDto;
import org.springframework.core.io.FileSystemResource;

public interface VideoService {

    /**
    * Creates an video in the database.
    *
    * @param videoDto component dto for the video
    * @param videoData raw data for the video
    * @return Component detail of the video created
    */
    ComponentDetailDto createVideo(VideoCreateDto videoDto, byte[] videoData);

    /**
    * Updates an video in the database.
    *
    * @param videoDto component dto for the video
    * @param videoData raw data for the video
    * @return Component detail of the video created
    */
    ComponentDetailDto updateVideo(VideoUpdateDto videoDto, byte[] videoData);

    /**
    * Gets video.
    *
    * @param id of the video to get
    * @return file resource
    */
    FileSystemResource getVideo(long id);
}
