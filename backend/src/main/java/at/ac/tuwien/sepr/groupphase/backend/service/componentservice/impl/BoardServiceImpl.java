package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
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

    public BoardServiceImpl(ComponentRepository componentRepository, BoardComponentValidator boardValidator,
            ComponentService componentService) {
        this.componentRepository = componentRepository;
        this.boardValidator = boardValidator;
        this.componentService = componentService;
    }

    @Override
    public ComponentDetailDto createBoard(BoardCreateDto boardDto) {
        LOG.trace("createBoard({})", boardDto);
        boardValidator.validateBoardComponent(boardDto, -1L);
        return setBoardComponent(boardDto, new Board());
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
        return componentService.setComponent(boardDto, board);
    }
}