package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;


@RestController
@RequestMapping("student")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return ResponseEntity.ok(createdStudent);
    }

    @GetMapping("{id}")
    public ResponseEntity<Student> getStudentInfoById(@PathVariable long id) {
        Student student = studentService.getStudentInfo(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    @GetMapping
    public ResponseEntity<Collection<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/age/{age}")
    public ResponseEntity<Collection<Student>> getStudentsByAge(@PathVariable int age) {
        Collection<Student> students = studentService.getStudentByAge(age);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/age/between")
    public ResponseEntity<Collection<Student>> getStudentsByAgeBetweenTheParameters(@RequestParam int min, @RequestParam int max) {
        Collection<Student> studentsResult = studentService.findStudentsByAgeBetweenTheParameters(min, max);
        return ResponseEntity.ok(studentsResult);
    }

    @GetMapping("{studentId}/faculty")
    public ResponseEntity<Faculty> getFacultyByStudentId(@PathVariable long studentId) {
        Faculty faculty = studentService.getFacultyByStudentId(studentId);
        if (faculty == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faculty);
    }


    @PutMapping("{id}")
    public ResponseEntity<Student> updateStudentById(@PathVariable long id, @RequestBody Student newStudent) {
        Student updatedStudent = studentService.updateStudent(id, newStudent);
        if (updatedStudent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteStudentById(@PathVariable long id) {
        if (studentService.getStudentInfo(id) == null) {
            return ResponseEntity.notFound().build();
        }
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }

}
