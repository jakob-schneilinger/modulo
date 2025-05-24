package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Board;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.BoardService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

@Service
public class BoardServiceImpl implements BoardService {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final BoardComponentValidator boardValidator;
    private final ComponentService componentService;

    public BoardServiceImpl(ComponentRepository componentRepository, BoardComponentValidator boardValidator,
            ComponentService componentService) {
        this.componentRepository = componentRepository;
        this.boardValidator = boardValidator;
        this.componentService = componentService;
    }

    @Override
    public ComponentDetailDto createBoard(BoardCreateDto boardDto) {
        LOG.trace("createBoard({})", boardDto);
        long userId = componentService.getUserId();
        boardValidator.validateBoardForCreation(boardDto, userId);
        return setBoardComponent(boardDto, new Board(), userId);
    }

    @Override
    @Transactional
    public ComponentDetailDto updateBoard(BoardUpdateDto boardDto) {
        LOG.trace("updateBoard({})", boardDto);

        Optional<Component> optionalComponent = componentRepository.findById(boardDto.id());
        Component component;

        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
        } else {
            throw new NotFoundException("Board with given ID does not exist");
        }

        if (!(component instanceof Board board)) {
            throw new ConflictException("Failed to update board",
                    List.of("Id given does not reference a component with the type board"));
        }

        long userId = componentService.getUserId();
        boardValidator.validateBoardForUpdate(boardDto, component, userId);
        return setBoardComponent(boardDto, board, userId);
    }

    private ComponentDetailDto setBoardComponent(BoardDto boardDto, Board board, long userId) {
        if (boardDto.name() != null) {
            board.setBoardName(boardDto.name());
        }
        return componentService.setComponent(boardDto, board, userId);
    }
}
