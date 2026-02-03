package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;


import java.util.*;

@Service
@Transactional
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;

    @Autowired
    public FacultyServiceImpl(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    public Faculty getFacultyInfo(long id) {
        return facultyRepository.findById(id).orElse(null);
    }

    public Collection<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    public Collection<Faculty> getFacultyByColor(String color) {
        return facultyRepository.findByColorIgnoreCase(color);
    }


    public Faculty updateFaculty(long id, Faculty newFaculty) {
        Faculty existingFaculty = facultyRepository.findById(id).orElse(null);
        if (existingFaculty == null) {
            return null;
        }
        if (newFaculty.getName() != null && !newFaculty.getName().isBlank()) {
            existingFaculty.setName(newFaculty.getName().trim());
        }
        if (newFaculty.getColor() != null && !newFaculty.getColor().isBlank()) {
            existingFaculty.setColor(newFaculty.getColor().trim());
        }
        return facultyRepository.save(existingFaculty);

    }

    public void deleteFaculty(long id) {
        facultyRepository.deleteById(id);
    }

    public Collection<Faculty> getFacultyByNameOrColor(String query) {
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(query, query);
    }

    public Collection<Student> getStudentsByFacultyId(long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId).orElse(null);
        if (faculty == null) {
            return new ArrayList<>();
        }
        Collection<Student> students = faculty.getStudents();
        if (students == null) {
            return new ArrayList<>();
        }
        return students;
    }

}
