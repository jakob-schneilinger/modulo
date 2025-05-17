package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

public interface ComponentDto {

    Long parentId();

    long width();

    long height();

    long column();

    long row();
}