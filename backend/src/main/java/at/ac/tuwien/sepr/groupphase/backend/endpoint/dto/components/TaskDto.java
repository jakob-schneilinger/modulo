package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.time.LocalDate;

public interface TaskDto extends ContainerDto {

    LocalDate startDate();

    LocalDate endDate();

    boolean completed();

    boolean repeating();
}
