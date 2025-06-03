package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.NoteCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.NoteDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.NoteUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Label;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Note;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.LabelService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.NoteService;
import at.ac.tuwien.sepr.groupphase.backend.validation.NoteComponentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final ComponentService componentService;
    private final NoteComponentValidator noteValidator;
    private final LabelService labelService;

    public NoteServiceImpl(ComponentRepository componentRepository, ComponentService componentService, NoteComponentValidator noteValidator, LabelService labelService) {
        this.componentRepository = componentRepository;
        this.componentService = componentService;
        this.noteValidator = noteValidator;
        this.labelService = labelService;
    }

    @Override
    public ComponentDetailDto createNote(NoteCreateDto noteDto) {
        LOG.trace("Creating new note({})", noteDto);
        noteValidator.validateNoteComponent(noteDto, -1L);
        return setNoteComponent(noteDto, new Note());
    }

    @Override
    public ComponentDetailDto updateNote(NoteUpdateDto noteDto) {
        LOG.trace("Updating note({})", noteDto);
        noteValidator.validateNoteComponent(noteDto, noteDto.id());

        Note note = componentRepository.findById(noteDto.id())
            .filter(c -> c instanceof Note)
            .map(c -> (Note) c)
            .orElseThrow(() -> new NotFoundException("Note with given ID does not exist"));

        return setNoteComponent(noteDto, note);
    }

    private ComponentDetailDto setNoteComponent(NoteDto noteDto, Note note) {
        LOG.trace("Setting noteComponent({})", noteDto);

        if (noteDto.labels() != null) {
            Set<Label> labels = noteDto.labels().stream()
                .map(labelService::setLabel)
                .collect(Collectors.toSet());
            note.setLabels(labels);
        }

        Optional.ofNullable(noteDto.title()).ifPresent(note::setTitle);
        return componentService.setComponent(noteDto, note);
    }
}
