package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.LabelDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Label;

public interface LabelService {

    /**
     * Sets a lable in the database.
     *
     * @param label to be set
     * @return lable that has been set
     */
    Label setLabel(LabelDto label);

    /**
     * Gets the label with given name.
     *
     * @param name of the label
     * @return label
     */
    LabelDto getLabel(String name);
}
