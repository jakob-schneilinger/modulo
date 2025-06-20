package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.LabelDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Label;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.LabelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing label operations.
 * This controller provides endpoints for setting and deleting labels.
 */
@RestController
@RequestMapping(value = "/api/v1/component/label")
public class LabelEndpoint {

    private final LabelService service;

    @Autowired
    public LabelEndpoint(LabelService labelService) {
        this.service = labelService;
    }

    /**
     * Sets a label in the database.
     *
     * @param labelDto to set
     * @return label that has been set
     */
    @PostMapping("")
    public ResponseEntity<LabelDto> setLabel(@RequestBody LabelDto labelDto) {
        Label label = service.setLabel(labelDto);
        return new ResponseEntity<>(new LabelDto(label.getName(), label.getColor()), HttpStatus.CREATED);
    }
}
