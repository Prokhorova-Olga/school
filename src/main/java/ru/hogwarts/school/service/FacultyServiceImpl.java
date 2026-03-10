package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(FacultyServiceImpl.class);
    private final FacultyRepository facultyRepository;


    @Autowired
    public FacultyServiceImpl(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        logger.info("Was invoked method for create faculty");
        return facultyRepository.save(faculty);
    }

    public Optional<Faculty> getFacultyInfo(long id) {
        logger.info("Was invoked method for get faculty info");
        return facultyRepository.findById(id);
    }

    public Collection<Faculty> getAllFaculties() {
        logger.info("Was invoked method for get all faculties");
        return facultyRepository.findAll();
    }

    public Collection<Faculty> getFacultyByColor(String color) {
        logger.info("Was invoked method for get faculty by color");
        return facultyRepository.findByColorIgnoreCase(color);
    }


    public Faculty updateFaculty(long id, Faculty newFaculty) {
        logger.info("Was invoked method for update faculty");
        Faculty existingFaculty = facultyRepository.findById(id).orElse(null);
        if (existingFaculty == null) {
            logger.warn("Faculty with id = {} not found", id);
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
        logger.info("Was invoked method for delete faculty");
        facultyRepository.deleteById(id);
    }

    public Collection<Faculty> getFacultyByNameOrColor(String query) {
        logger.info("Was invoked method for get faculty by name or color");
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(query, query);
    }

    public Collection<Student> getStudentsByFacultyId(long facultyId) {
        logger.info("Was invoked method for get students by faculty id");
        Faculty faculty = facultyRepository.findById(facultyId).orElse(null);
        if (faculty == null) {
            logger.warn("Faculty with facultyId = {} not found", facultyId);
            return new ArrayList<>();
        }
        Collection<Student> students = faculty.getStudents();
        if (students == null) {
            logger.warn("Students with facultyId = {} not found", facultyId);
            return new ArrayList<>();
        }
        return students;
    }

}
