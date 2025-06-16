package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ws.ComponentUpdateWsDto;

@Service
public class ComponentUpdateNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ComponentUpdateNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyComponentUpdate(ComponentUpdateWsDto update) {
        messagingTemplate.convertAndSend("/board/" + update.rootId(), update);
    }

    public void notifyRootAdded(ComponentDetailDto root) {
        // TODO: get all relevant usernames
        String[] userNames = new String[] { "jakob", "julian", "simon" };
        for (String name : userNames) {
            messagingTemplate.convertAndSend("/board/new/" + name, root);
        }
    }
}
