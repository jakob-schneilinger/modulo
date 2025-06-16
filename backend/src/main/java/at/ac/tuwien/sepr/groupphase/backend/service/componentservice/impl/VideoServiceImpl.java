package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.VideoCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.VideoUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Video;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.VideoService;
import at.ac.tuwien.sepr.groupphase.backend.validation.VideoComponentValidator;
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

@Service
public class VideoServiceImpl implements VideoService {

    @Value("${global.location.video}")
    private String videoPath;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final ComponentService componentService;
    private final VideoComponentValidator videoValidator;

    @Autowired
    public VideoServiceImpl(ComponentRepository componentRepository, VideoComponentValidator videoValidator,
            ComponentService componentService) {
        this.componentRepository = componentRepository;
        this.videoValidator = videoValidator;
        this.componentService = componentService;
    }

    @Override
    public ComponentDetailDto createVideo(VideoCreateDto videoDto, byte[] videoData) {
        LOG.trace("Creating video {}...", videoDto);

        videoValidator.validateVideo(videoDto, videoData, -1L);
        ComponentDetailDto result = componentService.setComponent(videoDto, new Video());

        if (videoData != null) {
            saveVideoData(result.id(), videoData);
        }

        return result;
    }

    @Override
    @Transactional
    public ComponentDetailDto updateVideo(VideoUpdateDto videoDto, byte[] videoData) {
        LOG.trace("Updating video {}...", videoDto);

        Component component = componentRepository.findById(videoDto.id())
                .filter(c -> c instanceof Video)
                .map(c -> (Video) c)
                .orElseThrow(() -> new NotFoundException("Video with given ID does not exist"));

        videoValidator.validateVideo(videoDto, videoData, component.getId());
        ComponentDetailDto result = componentService.setComponent(videoDto, component);

        if (videoData != null) {
            saveVideoData(result.id(), videoData);
        }

        return result;
    }

    @Override
    public FileSystemResource getVideo(long id) {
        LOG.trace("Getting video {}...", id);

        File video = new File(videoPath + id);

        if (!video.exists()) {
            throw new NotFoundException("Video with given ID does not exist");
        }

        return new FileSystemResource(video);
    }

    private void saveVideoData(long filename, byte[] videoData) {
        File dir = new File(videoPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Unable to create directory " + videoPath);
            }
        }

        try {
            File file = new File(videoPath, String.valueOf(filename));
            var stream = new FileOutputStream(file);
            stream.write(videoData);
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't store file. This should not happen. Please contact an administrator.");
        }
    }
}
