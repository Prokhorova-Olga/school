package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.*;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student getStudentInfo(long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Collection<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Collection<Student> getStudentByAge(int age) {
        return studentRepository.findByAge(age);
    }

    public Student updateStudent(long id, Student newStudent) {
        Student existingStudent = studentRepository.findById(id).orElse(null);
        if (existingStudent == null) {
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
        studentRepository.deleteById(id);
    }

    public Collection<Student> findStudentsByAgeBetweenTheParameters(int min, int max) {
        Collection<Student> studentsResult = studentRepository.findByAgeBetween(min, max);
        return studentsResult;
    }

    public Faculty getFacultyByStudentId(long studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return null;
        }
        Faculty faculty = student.getFaculty();
        return faculty;
    }
}
