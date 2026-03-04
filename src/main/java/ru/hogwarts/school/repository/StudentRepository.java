package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.hogwarts.school.model.AmountOfStudents;
import ru.hogwarts.school.model.Student;

import java.util.Collection;


public interface StudentRepository extends JpaRepository<Student, Long> {
    Collection<Student> findByAge(int age);

    Collection<Student> findByAgeBetween(int min, int max);

    @Query(value = "SELECT COUNT(*) as amount FROM student", nativeQuery = true)
    AmountOfStudents getAmountOfStudents();

    @Query(value = "SELECT AVG(age) as averageAge FROM student", nativeQuery = true)
    AmountOfStudents getAverageAgeOfStudents();

    @Query(value = "SELECT * FROM student as fiveMaxId ORDER BY id DESC LIMIT 5", nativeQuery = true)
    Collection<Student> getFiveMaxId();
}
