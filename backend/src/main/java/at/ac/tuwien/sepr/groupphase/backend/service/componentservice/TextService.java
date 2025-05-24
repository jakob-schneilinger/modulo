package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;

public interface TextService {

    /**
     * Creates a TextComponent in database.
     *
     * @param textCreateDto board to be created
     * @return Component detail of the TextComponent created
     */

    ComponentDetailDto createTextComponent(TextCreateDto textCreateDto);

    /**
     * Updates a Text component in database.
     *
     * @param textComponent to be updated
     * @return Component detail of the text component updated
     */
    ComponentDetailDto updateTextComponent(TextUpdateDto textComponent);
}
