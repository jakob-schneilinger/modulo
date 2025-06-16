package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Board;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.BoardService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.validation.BoardComponentValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

@Service
public class BoardServiceImpl implements BoardService {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final BoardComponentValidator boardValidator;
    private final ComponentService componentService;
    private final ComponentUpdateNotifier updateNotifier;

    public BoardServiceImpl(ComponentRepository componentRepository, BoardComponentValidator boardValidator,
            ComponentService componentService, ComponentUpdateNotifier updateNotifier) {
        this.componentRepository = componentRepository;
        this.boardValidator = boardValidator;
        this.componentService = componentService;
        this.updateNotifier = updateNotifier;
    }

    @Override
    public ComponentDetailDto createBoard(BoardCreateDto boardDto) {
        LOG.trace("createBoard({})", boardDto);
        boardValidator.validateBoardComponent(boardDto, -1L);
        return setBoardComponent(boardDto, new Board());
        // TODO: send new board update
        // updateNotifier.notifyRootAdded(new BoardDetailDto(0, null, null, 0, 0, 0, 0, null));
    }

    @Override
    @Transactional
    public ComponentDetailDto updateBoard(BoardUpdateDto boardDto) {
        LOG.trace("updateBoard({})", boardDto);
        boardValidator.validateBoardComponent(boardDto, boardDto.id());

        Board board = componentRepository.findById(boardDto.id())
            .filter(c -> c instanceof Board)
            .map(c -> (Board) c)
            .orElseThrow(() -> new NotFoundException("Board with given ID does not exist"));

        return setBoardComponent(boardDto, board);
    }

    private ComponentDetailDto setBoardComponent(BoardDto boardDto, Board board) {
        LOG.trace("setBoardComponent({})", boardDto);
        Optional.ofNullable(boardDto.name()).ifPresent(board::setBoardName);
        Optional.ofNullable(boardDto.depth()).ifPresent(board::setDepth);
        if (board.getDepth() == null) {
            board.setDepth(5);
        }
        return componentService.setComponent(boardDto, board);
    }
}