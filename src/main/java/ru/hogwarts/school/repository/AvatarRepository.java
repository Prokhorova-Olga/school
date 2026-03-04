package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestParam;
import ru.hogwarts.school.model.Avatar;

import java.util.Collection;
import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    Optional<Avatar> findByStudentId(Long studentId);

}
