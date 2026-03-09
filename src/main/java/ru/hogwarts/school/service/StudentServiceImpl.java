package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.model.AmountOfStudents;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.*;


@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);
    private final StudentRepository studentRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        logger.info("Was invoked method for create student");
        return studentRepository.save(student);
    }

    public Optional<Student> getStudentInfo(long id) {
        logger.info("Was invoked method for get student info");
        return studentRepository.findById(id);
    }

    public Collection<Student> getAllStudents() {
        logger.info("Was invoked method for get all students");
        return studentRepository.findAll();
    }

    public Collection<Student> getStudentByAge(int age) {
        logger.info("was invoked method get student by age");
        return studentRepository.findByAge(age);
    }

    public Student updateStudent(long id, Student newStudent) {
        logger.info("Was invoked method for update student");
        Student existingStudent = studentRepository.findById(id).orElse(null);
        if (existingStudent == null) {
            logger.warn("Student with id = {} not found", id);
            return null;
        }
        if (newStudent.getName() != null && !newStudent.getName().isBlank()) {
            existingStudent.setName(newStudent.getName().trim());
        }
        if (newStudent.getAge() > 0 && newStudent.getAge() < 150) {
            existingStudent.setAge(newStudent.getAge());
        }
        return studentRepository.save(existingStudent);

    }

    public void deleteStudent(long id) {
        logger.info("Was invoked method for delete student");
        studentRepository.deleteById(id);
    }

    public Collection<Student> findStudentsByAgeBetweenTheParameters(int min, int max) {
        logger.info("Was invoked method find students by age between the parameters");
        Collection<Student> studentsResult = studentRepository.findByAgeBetween(min, max);
        return studentsResult;
    }

    public Faculty getFacultyByStudentId(long studentId) {
        logger.info("Was invoked method for get faculty by student id");
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            logger.warn("Student with student id = {} not found", studentId);
            return null;
        }
        Faculty faculty = student.getFaculty();
        return faculty;

    }

    public Long findTotalStudents() {
        logger.info("Was invoked method find total students");
        AmountOfStudents result = studentRepository.getAmountOfStudents();
        return result.getAmount();
    }

    public Double findAverageAgeOfStudents() {
        logger.info("Was invoked method find average age of students");
        AmountOfStudents result = studentRepository.getAverageAgeOfStudents();
        return result.getAverageAge();
    }

    public Collection<Student> findFiveMaxIdStudents() {
        logger.info("Was invoked method find five max id students");
        Collection<Student> result = studentRepository.getFiveMaxId();
        return result;
    }


}
