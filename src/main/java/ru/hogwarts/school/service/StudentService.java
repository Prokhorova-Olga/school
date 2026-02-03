package ru.hogwarts.school.service;

import ru.hogwarts.school.model.Student;

import java.util.Collection;


public interface StudentService {

    Student createStudent(Student student);

    Student getStudentInfo(long id);

    Student updateStudent(long id, Student newStudent);

    void deleteStudent(long id);

    Collection<Student> getAllStudents();

    Collection<Student> getStudentByAge(int age);
}

