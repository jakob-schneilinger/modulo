package at.ac.tuwien.sepr.groupphase.backend.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.LabelDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Board;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.MyCalendar;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Image;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Note;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Task;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.NoteDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Text;

import java.util.ArrayList;
import java.util.List;

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
        return new BoardDetailDto(board.getId(), getParentId(board), board.getBoardName(), board.getDepth(), board.getWidth(), board.getHeight(),
                board.getColumn(), board.getRow(), children);
    }

    public static TaskDetailDto visit(Task task, MappingDepth depth) {
        List<ComponentDetailDto> children = switch (depth) {
            case DEEP -> task.getChildren().stream()
                .map(child -> child.accept(depth))
                .toList();
            case SHALLOW -> List.of();
        };
        return new TaskDetailDto(task.getId(), getParentId(task), task.getTaskName(), task.getWidth(), task.getHeight(),
            task.getColumn(), task.getRow(), children, task.getStartDate(), task.getEndDate(), task.isCompleted(),
            task.isRepeatable());
    }

    public static TextDetailDto visit(Text text) {
        return new TextDetailDto(text.getId(), getParentId(text), text.getContent(), text.getWidth(), text.getHeight(),
                text.getColumn(), text.getRow());
    }

    public static ImageDetailDto visit(Image image) {
        return new ImageDetailDto(image.getId(), getParentId(image), image.getWidth(), image.getHeight(), image.getColumn(),
                image.getRow());
    }

    public static NoteDetailDto visit(Note note, MappingDepth depth) {
        List<ComponentDetailDto> children = switch (depth) {
            case DEEP -> note.getChildren().stream()
                .map(child -> child.accept(depth))
                .toList();
            case SHALLOW -> List.of();
        };
        return new NoteDetailDto(note.getId(), getParentId(note), note.getTitle(),
            note.getLabels().stream().map(entity -> new LabelDto(entity.getName(), entity.getColor())).toList(),
            note.getWidth(), note.getHeight(), note.getColumn(), note.getRow(),
            children);
    }

    public static CalendarDetailDto visit(MyCalendar myCalendar) {
        CalendarDetailDto temp = new CalendarDetailDto(myCalendar.getId(), getParentId(myCalendar), myCalendar.getWidth(),
            myCalendar.getHeight(), myCalendar.getColumn(),
            myCalendar.getRow(),
            myCalendar.getEntries() == null ? new ArrayList<>() : MyCalendar.getCalendarEntries(myCalendar.getEntries()));

        return temp;
    }

    private static Long getParentId(Component component) {
        if (component.getParents() != null && !component.getParents().isEmpty()) {
            return component.getParents().getFirst().getId();
        }
        return null;
    }
}