package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;

import java.util.List;

public interface ComponentService {

    /**
     * Gets the details of the component with given id including all children.
     *
     * @param id id of the component to get
     * @return Component detail of the component with all subcomponents
     */
    ComponentDetailDto getComponentById(long id);

    /**
     * Gets all details of the root components of a user (all components without a parent).
     *
     * @return List of component details of the root components
     */
    List<ComponentDetailDto> getRootComponents();

    /**
    * Deletes a component from the database.
    *
    * @param id of component to delete
    */
    void deleteComponent(Long id);

    /**
     * Updates a Component.
     *
     * @param dto update data
     * @return updated component
     */
    ComponentDetailDto updateComponent(ComponentUpdateDto dto);

    /**
     * Helper method to save components.
     *
     * @param componentDto dto of the component
     * @param component entity of the component
     * @return details of the component
     */
    ComponentDetailDto setComponent(ComponentDto componentDto, Component component);

}
