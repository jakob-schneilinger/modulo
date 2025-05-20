package at.ac.tuwien.sepr.groupphase.backend.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Board;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Text;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Visitor class for Components that transforms an entity to a corresponding dto.
 */
public class ComponentEntityToDtoMapper {

    public static BoardDetailDto visit(Board board, MappingDepth depth) {
        List<ComponentDetailDto> children = switch (depth) {
            case DEEP -> board.getChildren().stream()
                .map(child -> child.accept(depth))
                .toList();
            case SHALLOW -> List.of();
        };
        return new BoardDetailDto(board.getId(), board.getBoardName(), board.getWidth(), board.getHeight(), board.getColumn(), board.getRow(), children);
    }

    public static TextDetailDto visit(Text text) {
        return new TextDetailDto(text.getText(), text.getName(), text.getId(), text.getWidth(), text.getHeight(), text.getColumn(), text.getRow(), text.getFontSize());
    }
}
