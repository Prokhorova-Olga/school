package ru.hogwarts.school.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("student")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@Valid @RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return ResponseEntity.ok(createdStudent);
    }

    @GetMapping("{id}")
    public ResponseEntity<Student> getStudentInfoById(@PathVariable long id) {
        return studentService.getStudentInfo(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

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
    public ResponseEntity<Student> updateStudentById(@PathVariable long id, @Valid @RequestBody Student newStudent) {
        Student updatedStudent = studentService.updateStudent(id, newStudent);
        if (updatedStudent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteStudentById(@PathVariable long id) {
        if (studentService.getStudentInfo(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/amount-students")
    public ResponseEntity<Long> getStudentCount() {
        Long count = studentService.findTotalStudents();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/average-age")
    public ResponseEntity<Double> getAverageAge() {
        Double age = studentService.findAverageAgeOfStudents();
        return ResponseEntity.ok(age);
    }

    @GetMapping("/average-age-of-students")
    public ResponseEntity<Double> getAverageAgeOfStudents() {
        Double averageAge = studentService.getAllStudents().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0);
        return ResponseEntity.ok(averageAge);

    }

    @GetMapping("/max-id")
    public ResponseEntity<Collection<Student>> getFiveMaxIdStudents() {
        Collection<Student> result = studentService.findFiveMaxIdStudents();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/names-starts-with-A")
    public ResponseEntity<List<String>> getStudentNameStartingWithA() {
        List<String> names = studentService.getAllStudents().stream()
                .filter(student -> student.getName().toUpperCase().startsWith("A"))
                .map(student -> student.getName().toUpperCase())
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(names);
    }

}
