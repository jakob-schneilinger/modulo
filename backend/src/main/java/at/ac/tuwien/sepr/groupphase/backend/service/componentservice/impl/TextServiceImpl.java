package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Text;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.TextService;
import at.ac.tuwien.sepr.groupphase.backend.validation.TextComponentValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

@Service
public class TextServiceImpl implements TextService {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final TextComponentValidator textValidator;
    private final ComponentService componentService;

    public TextServiceImpl(ComponentRepository componentRepository, TextComponentValidator textValidator,
                            ComponentService componentService) {
        this.componentRepository = componentRepository;
        this.textValidator = textValidator;
        this.componentService = componentService;
    }

    @Override
    @Transactional
    public ComponentDetailDto createTextComponent(TextCreateDto dto) {
        LOG.trace("createText({})", dto);
        textValidator.validateTextComponent(dto, -1L);
        return setTextComponent(dto, new Text());
    }

    @Override
    @Transactional
    public ComponentDetailDto updateTextComponent(TextUpdateDto dto) {
        LOG.trace("updateText({})", dto);
        LOG.warn("{} the id of the text", dto.id());

        textValidator.validateTextComponent(dto, dto.id());

        Text text = componentRepository.findById(dto.id())
            .filter(c -> c instanceof Text)
            .map(c -> (Text) c)
            .orElseThrow(() -> new NotFoundException("Text not found: " + dto.id()));

        return setTextComponent(dto, text);
    }

    private ComponentDetailDto setTextComponent(TextDto textDto, Text text) {
        LOG.trace("setTextComponent({})", textDto);
        Optional.ofNullable(textDto.content()).ifPresent(text::setContent);
        return componentService.setComponent(textDto, text);
    }
}