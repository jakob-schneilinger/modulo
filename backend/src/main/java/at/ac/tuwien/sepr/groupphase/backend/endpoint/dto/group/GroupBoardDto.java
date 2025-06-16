package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group;

public record GroupBoardDto(long groupId, String name, long boardId, PermissionDto permission) {
}
