package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * REST controller for managing component operations.
 * This controller provides endpoints for creating, updating, retrieving, and deleting components.
 * Specifically, it includes operations for managing boards within the application.
 */
@RestController
@RequestMapping(value = "/api/v1/component")
public class ComponentEndpoint {

    private final ComponentService service;

    @Autowired
    public ComponentEndpoint(ComponentService componentService) {
        this.service = componentService;
    }

    /**
     * Gets the details of the component with given id including all children.
     *
     * @param id of the component to get
     * @return component detail of component with all subcomponents
     */
    @PermitAll // TODO: fix this
    @GetMapping("{id}")
    public ResponseEntity<ComponentDetailDto> getComponent(@PathVariable("id") long id) {
        return new ResponseEntity<>(service.getComponentById(id), HttpStatus.OK);
    }

    /**
     * Gets a list of all root components (components without a parent) NOT including children.
     *
     * @return list of the details of the root components
     */
    @PermitAll // TODO: fix this
    @GetMapping()
    public ResponseEntity<List<ComponentDetailDto>> getRootComponents() {
        return new ResponseEntity<>(service.getRootComponents(), HttpStatus.OK);
    }

    /**
     * Updates a component.
     *
     * @param dto update data
     * @return updated component
     */
    @PermitAll
    @PutMapping()
    public ResponseEntity<ComponentDetailDto> updateComponent(@RequestBody ComponentUpdateDto dto) {
        return new ResponseEntity<>(service.updateComponent(dto), HttpStatus.OK);
    }

    /**
     * Deletes a component.
     *
     * @param id of component to delete
     * @return void
     */
    @PermitAll // TODO: fix this
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteComponent(
            @PathVariable("id") long id) {
        service.deleteComponent(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}