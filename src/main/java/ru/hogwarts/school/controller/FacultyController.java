package ru.hogwarts.school.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.LongStream;


@RestController
@RequestMapping("faculty")
public class FacultyController {

    private final FacultyService facultyService;

    @Autowired
    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }


    @PostMapping
    public ResponseEntity<Faculty> createFaculty(@Valid @RequestBody Faculty faculty) {
        Faculty createdFaculty = facultyService.createFaculty(faculty);
        return ResponseEntity.ok(createdFaculty);
    }

    @GetMapping("{id}")
    public ResponseEntity<Faculty> getFacultyInfoById(@PathVariable long id) {
        return facultyService.getFacultyInfo(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Collection<Faculty>> getFaculties(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(facultyService.getFacultyByNameOrColor(search));
        }
        return ResponseEntity.ok(facultyService.getAllFaculties());
    }

    @GetMapping("/color/{color}")
    public ResponseEntity<Collection<Faculty>> getFacultyByColor(@PathVariable String color) {
        return ResponseEntity.ok(facultyService.getFacultyByColor(color));
    }

    @GetMapping("{facultyId}/students")
    public ResponseEntity<Collection<Student>> getStudentsByFacultyId(@PathVariable long facultyId) {
        return ResponseEntity.ok(facultyService.getStudentsByFacultyId(facultyId));
    }


    @PutMapping("{id}")
    public ResponseEntity<Faculty> updateFacultyById(@PathVariable long id, @Valid @RequestBody Faculty newFaculty) {
        Faculty updatedFaculty = facultyService.updateFaculty(id, newFaculty);
        if (updatedFaculty == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedFaculty);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteFacultyById(@PathVariable long id) {
        if (facultyService.getFacultyInfo(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/the-longest-faculty-name")
    public ResponseEntity<String> getTheLongestFacultyName() {
        String name = facultyService.getAllFaculties().stream()
                .map(Faculty::getName)
                .max(Comparator.comparingInt(String::length))
                .orElse("");
        return ResponseEntity.ok(name);

    }

    @GetMapping("/fast-return-of-an-integer-expression")
    public Long getIntegerValue() {
        return LongStream.rangeClosed(1, 1_000_000)
                .parallel()
                .sum();
    }
}


