package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.LabelDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Label;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.LabelRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.LabelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Service
public class LabelServiceImpl implements LabelService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final LabelRepository labelRepository;

    public LabelServiceImpl(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    @Override
    public Label setLabel(LabelDto labelDto) {
        LOG.trace("Setting label: {}", labelDto);
        validateLabel(labelDto);

        Label label = new Label();
        label.setName(labelDto.name());
        if (labelDto.color() != null) {
            label.setColor(labelDto.color());
        }

        labelRepository.save(label);

        return label;
    }

    @Override
    public LabelDto getLabel(String name) {
        return labelRepository.findById(name).map(label -> new LabelDto(label.getName(), label.getColor()))
            .orElseThrow(() -> new NotFoundException("Label not found with name: " + name));
    }

    private void validateLabel(LabelDto label) {
        LOG.trace("Validating label: {}", label);
        List<String> errors = new ArrayList<>();

        if (label.name() == null || label.name().isEmpty()) {
            errors.add("Name is null or empty");
        } else if (label.name().length() > 255) {
            errors.add("Name is too long");
        }

        if (label.color() != null && !isValidHexColor(label.color())) {
            errors.add("Invalid color");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation of label failed", errors);
        }
    }

    private boolean isValidHexColor(String color) {
        LOG.trace("Validating hex color: {}", color);
        return color.matches("^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$");
    }
}
