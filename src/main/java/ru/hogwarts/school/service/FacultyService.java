package ru.hogwarts.school.service;

import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.util.Collection;


public interface FacultyService {

    Faculty createFaculty(Faculty faculty);

    Faculty getFacultyInfo(long id);

    Faculty updateFaculty(long id, Faculty newFaculty);

    void deleteFaculty(long id);

    Collection<Faculty> getAllFaculties();

    Collection<Faculty> getFacultyByColor(String color);

    Collection<Faculty> getFacultyByNameOrColor(String query);

    Collection<Student> getStudentsByFacultyId(long facultyId);

}
