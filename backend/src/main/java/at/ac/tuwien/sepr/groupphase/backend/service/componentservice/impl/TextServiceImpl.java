package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Text;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.TextService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class TextServiceImpl implements TextService {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final ComponentValidator componentValidator;
    private final ComponentService componentService;

    public TextServiceImpl(ComponentRepository componentRepository, ComponentValidator componentValidator,
                            ComponentService componentService) {
        this.componentRepository = componentRepository;
        this.componentValidator = componentValidator;
        this.componentService = componentService;
    }

    @Override
    @Transactional
    public ComponentDetailDto createTextComponent(TextCreateDto dto) {
        LOG.trace("createText({})", dto);


        long userId = componentService.getUserId();

        List<String> errors = componentValidator.validateComponent(dto, userId, -1L);

        if (!errors.isEmpty()) {
            throw new ValidationException("Errors in text component creation", errors);
        }

        Text text = new Text();
        text.setName(dto.name());
        text.setText(dto.text());
        text.setFontSize(dto.fontSize());

        return componentService.setComponent(dto, text, userId);
    }

    @Override
    @Transactional
    public ComponentDetailDto updateTextComponent(TextUpdateDto dto) {
        LOG.trace("updateText({})", dto);
        LOG.warn("{} the id of the text", dto.id());

        long userId = componentService.getUserId();
        List<String> errors = componentValidator.validateComponent(dto, userId, dto.id());

        if (!errors.isEmpty()) {
            throw new ValidationException("Errors in text component creation", errors);
        }

        Text text = componentRepository.findById(dto.id())
            .filter(c -> c instanceof Text)
            .map(c -> (Text) c)
            .orElseThrow(() -> new NotFoundException("Text not found: " + dto.id()));

        if (!text.getOwnerId().equals(userId)) {
            throw new RuntimeException("User is not owner of this component");
        }
        text.setName(dto.name());
        text.setText(dto.text());
        text.setFontSize(dto.fontSize());

        return componentService.setComponent(dto, text, userId);
    }
}
