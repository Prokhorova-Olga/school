package ru.hogwarts.school.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentController studentController;
    @Autowired
    private AvatarRepository avatarRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private FacultyRepository facultyRepository;
    @Autowired
    TestRestTemplate testRestTemplate;

    @BeforeEach
    void clearDatabase() {
        avatarRepository.deleteAll();
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    public void createStudent_shouldReturnCreatedStudentWithId() {
        //Arrange
        Student student = new Student();
        student.setName("Max");
        student.setAge(15);

        //Act
        String url = "http://localhost:" + port + "/student";
        ResponseEntity<Student> response = testRestTemplate.postForEntity(url, student, Student.class);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Student createdStudent = response.getBody();
        assertThat(createdStudent).isNotNull();
        assertThat(createdStudent.getId()).isPositive();
        assertThat(createdStudent.getName()).isEqualTo("Max");
        assertThat(createdStudent.getAge()).isEqualTo(15);

        //проверка состояния базы данных
        Optional<Student> savedStudent = studentRepository.findById(createdStudent.getId());
        Student foundStudent = savedStudent.orElseThrow(() -> new AssertionError("Студент не найден в базе данных"));
        assertThat(foundStudent.getId()).isEqualTo(createdStudent.getId());
        assertThat(foundStudent.getName()).isEqualTo("Max");
        assertThat(foundStudent.getAge()).isEqualTo(15);

    }
    @Test
    public void createStudent_shouldReturn400_whenStudentNameIsBlank() {
        //Arrange
        Student noNameStudent = new Student();
        noNameStudent.setName("");
        noNameStudent.setAge(18);


       //Act
        String url = "http://localhost:" + port + "/student";
        ResponseEntity<Student> response = testRestTemplate.postForEntity(url, noNameStudent, Student.class);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(0);

    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 155})
    public void createStudent_shouldReturn400_whenAgeIsParametersIncorrect(int age) {
        //Arrange
        Student inappropriateStudent = new Student();
        inappropriateStudent.setName("TestStudent");
        inappropriateStudent.setAge(age);

        //Act
        String url = "http://localhost:" + port + "/student";
        ResponseEntity<Student> response = testRestTemplate.postForEntity(url, inappropriateStudent, Student.class);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(0);

    }

    @Test
    public void enterTheIdStudent_shouldReturnStudentWithId() {
        //Arrange
        Student student = new Student();
        student.setName("Max");
        student.setAge(20);
        studentRepository.save(student);
        Long idStudent = student.getId();

        //Act
        String url = "http://localhost:" + port + "/student/{id}";
        ResponseEntity<Student> response = testRestTemplate.getForEntity(url, Student.class, idStudent);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Student createdStudent = response.getBody();
        assertThat(response).isNotNull();
        assertThat(createdStudent.getId()).isEqualTo(idStudent);
        assertThat(createdStudent.getName()).isEqualTo("Max");
        assertThat(createdStudent.getAge()).isEqualTo(20);

        //проверка состояния базы данных
        Optional<Student> updateStudent = studentRepository.findById(idStudent);
        assertThat(updateStudent).isPresent();
        Student dbStudent = updateStudent.get();
        assertThat(dbStudent.getName()).isEqualTo("Max");
        assertThat(dbStudent.getAge()).isEqualTo(20);

    }


    @Test
    public void getStudentById_shouldReturn404_whenStudentNotFound() {
        //Arrange
        Student student = new Student();
        student.setName("Max");
        student.setAge(20);
        studentRepository.save(student);
        Long nonExistentId = -1L;


        //Act
        String url = "http://localhost:" + port + "/student/{id}";
        ResponseEntity<Student> response = testRestTemplate.getForEntity(url, Student.class, nonExistentId);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(1);
        assertThat(studentRepository.existsById(nonExistentId)).isFalse();
        Optional<Student> existingStudent = studentRepository.findById(student.getId());
        assertThat(existingStudent).isPresent();
        Student dbStudent = existingStudent.get();
        assertThat(dbStudent.getName()).isEqualTo("Max");
        assertThat(dbStudent.getAge()).isEqualTo(20);

    }

    @Test
    public void updateStudentById_whenEnterIdAndNewData_theModifiedStudentWithThatIdIsReturned() {
        //Arrange
        Student student = new Student();
        student.setName("Olga");
        student.setAge(18);
        studentRepository.save(student);
        Long idStudent = student.getId();
        Student modifiedStudent = new Student();
        modifiedStudent.setName("Olga Alekseevna");
        modifiedStudent.setAge(20);


        //Act
        String url = "http://localhost:" + port + "/student/{id}";
        ResponseEntity<Student> response = testRestTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(modifiedStudent), Student.class, idStudent);


        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


        //проверка тела ответа
        Student returnedStudent = response.getBody();
        assertThat(returnedStudent).isNotNull();
        assertThat(returnedStudent.getId()).isEqualTo(idStudent);
        assertThat(returnedStudent.getName()).isEqualTo("Olga Alekseevna");
        assertThat(returnedStudent.getAge()).isEqualTo(20);


        //проверка состояния базы данных
        Optional<Student> saveStudent = studentRepository.findById(returnedStudent.getId());
        Student founStudent = saveStudent.orElseThrow(() -> new AssertionError("Студент не найден в базе данных"));
        assertThat(founStudent.getName()).isEqualTo("Olga Alekseevna");
        assertThat(founStudent.getAge()).isEqualTo(20);
    }

    @Test
    public void updateStudentById_shouldReturn404_whenStudentNotFound() {
        //Arrange
        Student student = new Student();
        student.setName("Max");
        student.setAge(20);
        studentRepository.save(student);
        Long nonExistentId = -1L;
        Student dummyStudent = new Student();
        dummyStudent.setName("Dummy");
        dummyStudent.setAge(15);


        //Act
        String url = "http://localhost:" + port + "/student/{id}";
        HttpEntity<Student> requestEntity = new HttpEntity<>(dummyStudent);
        ResponseEntity<Student> response = testRestTemplate.exchange(url, HttpMethod.PUT, requestEntity, Student.class, nonExistentId);


        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(1);
        assertThat(studentRepository.existsById(nonExistentId)).isFalse();
        Optional<Student> existingStudent = studentRepository.findById(student.getId());
        assertThat(existingStudent).isPresent();
        Student dbStudent = existingStudent.get();
        assertThat(dbStudent.getName()).isEqualTo("Max");
        assertThat(dbStudent.getAge()).isEqualTo(20);

    }

    @Test
    public void updateStudentById_shouldReturn400_whenStudentNameIsBlank() {
        //Arrange
        Student student = new Student();
        student.setName("Olga");
        student.setAge(18);
        studentRepository.save(student);
        Long idStudent = student.getId();
        Student noNameStudent = new Student();
        noNameStudent.setName("");
        noNameStudent.setAge(20);

        //Act
        String url = "http://localhost:" + port + "/student/{id}";
        HttpEntity<Student> requestEntity = new HttpEntity<>(noNameStudent);
        ResponseEntity<Student> response = testRestTemplate.exchange(url, HttpMethod.PUT, requestEntity, Student.class, idStudent);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(1);
        Optional<Student> existingStudent = studentRepository.findById(student.getId());
        assertThat(existingStudent).isPresent();
        Student dbStudent = existingStudent.get();
        assertThat(dbStudent.getName()).isEqualTo("Olga");
        assertThat(dbStudent.getAge()).isEqualTo(18);

    }


    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 151})
    public void updateStudentById_shouldReturn400_whenAgeIsParametersIncorrect(int age) {
        //Arrange
        Student student = new Student();
        student.setName("Olga");
        student.setAge(18);
        studentRepository.save(student);
        Long idStudent = student.getId();
        Student inappropriateStudent = new Student();
        inappropriateStudent.setName("Olga");
        inappropriateStudent.setAge(age);


        //Act
        String url = "http://localhost:" + port + "/student/{id}";
        HttpEntity<Student> requestEntity = new HttpEntity<>(inappropriateStudent);
        ResponseEntity<Student> response = testRestTemplate.exchange(url, HttpMethod.PUT, requestEntity, Student.class, idStudent);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(1);
        Optional<Student> existingStudent = studentRepository.findById(student.getId());
        assertThat(existingStudent).isPresent();
        Student dbStudent = existingStudent.get();
        assertThat(dbStudent.getName()).isEqualTo("Olga");
        assertThat(dbStudent.getAge()).isEqualTo(18);

    }


    @Test
    public void deleteStudentById_whenEnterTheIdStudent_theStudentIsRemovedFromTheDatabaseWithThatId() {
        //Arrange
        Student student = new Student();
        student.setName("Max");
        student.setAge(34);
        studentRepository.save(student);
        Long idStudent = student.getId();

        //Act
        String url = "http://localhost:" + port + "/student/{id}";
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class, idStudent);

        //Assert
        // проверка Http - ответа
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        assertThat(deleteResponse.getBody()).isNull();

        //проверка состояния базы данных
        Optional<Student> foundStudent = studentRepository.findById(idStudent);
        assertThat(foundStudent).isEmpty();

    }

    @Test
    public void deleteStudentById_shouldReturn404_whenStudentNotFound() {
        //Arrange
        Student student = new Student();
        student.setName("Max");
        student.setAge(20);
        studentRepository.save(student);
        Long nonExistentId = -1L;


        //Act
        String url = "http://localhost:" + port + "/student/{id}";
        ResponseEntity<Void> response = testRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class, nonExistentId);


        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(1);
        assertThat(studentRepository.existsById(nonExistentId)).isFalse();
        Optional<Student> existingStudent = studentRepository.findById(student.getId());
        assertThat(existingStudent).isPresent();
        Student dbStudent = existingStudent.get();
        assertThat(dbStudent.getName()).isEqualTo("Max");
        assertThat(dbStudent.getAge()).isEqualTo(20);

    }


    @Test
    public void enterTheAgeStudent_shouldReturnFilteredStudentsByAge() {
        //Arrange
        Student studentOne = new Student();
        studentOne.setName("Olga");
        studentOne.setAge(25);
        studentRepository.save(studentOne);
        Long studentOneId = studentOne.getId();

        Student studentTwo = new Student();
        studentTwo.setName("Anna");
        studentTwo.setAge(24);
        studentRepository.save(studentTwo);
        Long studentTwoId = studentTwo.getId();


        Student studentThree = new Student();
        studentThree.setName("Yana");
        studentThree.setAge(25);
        studentRepository.save(studentThree);
        Long studentThreeId = studentThree.getId();

        //Act
        String url = "http://localhost:" + port + "/student/age/{age}";
        int age = 25;
        ResponseEntity<Student[]> response = testRestTemplate.getForEntity(url, Student[].class, age);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Student[] returnedStudents = response.getBody();
        assertThat(returnedStudents).isNotNull();
        assertThat(returnedStudents).hasSize(2);
        assertThat(returnedStudents).extracting(Student::getId).containsExactlyInAnyOrder(studentOneId, studentThreeId);
        assertThat(returnedStudents).extracting(Student::getName).containsExactlyInAnyOrder("Olga", "Yana");
        assertThat(returnedStudents).extracting(Student::getAge).containsExactlyInAnyOrder(25, 25);
        assertThat(returnedStudents).extracting(Student::getId).doesNotContain(studentTwo.getId());

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(3);

    }

    @Test
    public void getStudentsByAge_shouldReturnEmptyList_whenNoStudentWithSuchAge() {
        //Arrange
        Student student = new Student();
        student.setName("Nina");
        student.setAge(10);
        studentRepository.save(student);
        Long studentId = student.getId();

        //Act
        String url = "http://localhost:" + port + "/student/age/{age}";
        int age = 15;
        ResponseEntity<Student[]> response = testRestTemplate.getForEntity(url, Student[].class, age);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Student[] returnedResponse = response.getBody();
        assertThat(returnedResponse).isNotNull();
        assertThat(returnedResponse).isEmpty();

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(1);

    }


    @Test
    public void shouldReturnFilteredStudents_whenSearchMinAndMaxAgeParam() {
        //Arrange
        Student studentOne = new Student();
        studentOne.setName("Max");
        studentOne.setAge(20);
        studentRepository.save(studentOne);
        Long studentOneId = studentOne.getId();

        Student studentTwo = new Student();
        studentTwo.setName("Marina");
        studentTwo.setAge(15);
        studentRepository.save(studentTwo);
        Long studentTwoId = studentTwo.getId();

        Student studentThree = new Student();
        studentThree.setName("Masha");
        studentThree.setAge(16);
        studentRepository.save(studentThree);
        Long studentThreeId = studentThree.getId();

        //Act
        String url = "http://localhost:" + port + "/student/age/between?min={min}&max={max}";
        int minAge = 14;
        int maxAge = 18;
        ResponseEntity<Student[]> response = testRestTemplate.getForEntity(url, Student[].class, minAge, maxAge);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Student[] returnedStudents = response.getBody();
        assertThat(returnedStudents).isNotNull();
        assertThat(returnedStudents).hasSize(2);
        assertThat(returnedStudents).extracting(Student::getId).containsExactlyInAnyOrder(studentTwoId, studentThreeId);
        assertThat(returnedStudents).extracting(Student::getName).containsExactlyInAnyOrder("Marina", "Masha");
        assertThat(returnedStudents).extracting(Student::getAge).containsExactlyInAnyOrder(15, 16);
        assertThat(returnedStudents).extracting(Student::getId).doesNotContain(studentOne.getId());

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(3);
    }

    @Test
    public void findStudentsByAgeBetweenTheParameters_shouldReturn200andEmptyList_whenAgeIsParametersIncorrect() {
        //Arrange
        Student student = new Student();
        student.setName("Olga");
        student.setAge(18);
        studentRepository.save(student);
        int minAge = 20;
        int maxAge = 10;

        //Act
        String url = "http://localhost:" + port + "/student/age/between?min={min}&max={max}";
        ResponseEntity<Student[]> response = testRestTemplate.getForEntity(url, Student[].class, minAge, maxAge);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();


        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(1);
        Optional<Student> existingStudent = studentRepository.findById(student.getId());
        assertThat(existingStudent).isPresent();
        Student dbStudent = existingStudent.get();
        assertThat(dbStudent.getName()).isEqualTo("Olga");
        assertThat(dbStudent.getAge()).isEqualTo(18);

    }


    @Test
    public void getFacultyByStudentId_enterTheIdStudent_shouldReturnTheStudentFaculty() {
        //Arrange
        Faculty facultyOne = new Faculty();
        facultyOne.setName("Строительный");
        facultyOne.setColor("Белый");
        facultyRepository.save(facultyOne);
        Long facultyOneId = facultyOne.getId();

        Faculty facultyTwo = new Faculty();
        facultyTwo.setName("Архитектурный");
        facultyTwo.setColor("Серый");
        facultyRepository.save(facultyTwo);
        Long facultyTwoId = facultyTwo.getId();


        Student student = new Student();
        student.setName("Misha");
        student.setAge(22);
        student.setFaculty(facultyOne);
        studentRepository.save(student);
        Long studentId = student.getId();


        //Act
        String url = "http://localhost:" + port + "/student/{studentId}/faculty";
        ResponseEntity<Faculty> response = testRestTemplate.getForEntity(url, Faculty.class, studentId);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Faculty returnedFaculty = response.getBody();
        assertThat(returnedFaculty).isNotNull();
        assertThat(returnedFaculty.getId()).isEqualTo(facultyOneId);
        assertThat(returnedFaculty.getName()).isEqualTo("Строительный");
        assertThat(returnedFaculty.getColor()).isEqualTo("Белый");
        assertThat(returnedFaculty.getId()).isNotEqualTo(facultyTwoId);

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(2);
        Student dbStudent = studentRepository.findById(studentId).orElseThrow();
        assertThat(dbStudent.getFaculty().getId()).isEqualTo(facultyOneId);

    }

    @Test
    public void getFacultyByStudentId_shouldReturn404_whenFacultyNotFound() {
        //Arrange
        Student student = new Student();
        student.setName("Misha");
        student.setAge(22);
        studentRepository.save(student);
        Long studentId = student.getId();

        //Act
        String url = "http://localhost:" + port + "/student/{studentId}/faculty";
        ResponseEntity<Faculty> response = testRestTemplate.getForEntity(url, Faculty.class, studentId);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(1);
        assertThat(studentRepository.findById(studentId).get().getFaculty()).isNull();

    }

}



