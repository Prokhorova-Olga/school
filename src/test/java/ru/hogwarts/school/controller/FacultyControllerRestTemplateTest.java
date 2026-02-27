package ru.hogwarts.school.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class FacultyControllerRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FacultyController facultyController;
    @Autowired
    private AvatarRepository avatarRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private FacultyRepository facultyRepository;
    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    public void clearDataBase() {
        avatarRepository.deleteAll();
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    public void createFaculty_shouldReturnCreatedFacultyWithId() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Белый");

        //Act
        String url = "http://localhost:" + port + "/faculty";
        ResponseEntity<Faculty> response = testRestTemplate.postForEntity(url, faculty, Faculty.class);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверяем тело ответа
        Faculty createdFaculty = response.getBody();
        assertThat(createdFaculty).isNotNull();
        assertThat(createdFaculty.getId()).isPositive();
        assertThat(createdFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(createdFaculty.getColor()).isEqualTo("Белый");

        //проверка состояния базы данных
        Optional<Faculty> savedFaculty = facultyRepository.findById(createdFaculty.getId());
        Faculty foundFaculty = savedFaculty.orElseThrow(() -> new AssertionError("Факультет не найден в базе данных"));
        assertThat(foundFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(foundFaculty.getColor()).isEqualTo("Белый");

    }

    @Test
    public void createFaculty_shouldReturn400_whenFacultyNameIsBlank() {
        //Arrange
        Faculty noNameFaculty = new Faculty();
        noNameFaculty.setName("");
        noNameFaculty.setColor("Синий");


        //Act
        String url = "http://localhost:" + port + "/faculty";
        ResponseEntity<Faculty> response = testRestTemplate.postForEntity(url, noNameFaculty, Faculty.class);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(0);

    }

    @Test
    public void createFaculty_shouldReturn400_whenFacultyColorIsBlank() {
        //Arrange
        Faculty noColorFaculty = new Faculty();
        noColorFaculty.setName("Строительный");
        noColorFaculty.setColor("");


        //Act
        String url = "http://localhost:" + port + "/faculty";
        ResponseEntity<Faculty> response = testRestTemplate.postForEntity(url, noColorFaculty, Faculty.class);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(0);

    }

    @Test
    public void getFacultyById_shouldReturnFacultyWithId() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Белый");
        facultyRepository.save(faculty);
        Long facultyId = faculty.getId();

        //Act
        String url = "http://localhost:" + port + "/faculty/{id}";
        ResponseEntity<Faculty> response = testRestTemplate.getForEntity(url, Faculty.class, facultyId);

        //Assert
        //проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


        //проверка тела ответа
        Faculty returnedFaculty = response.getBody();
        assertThat(returnedFaculty).isNotNull();
        assertThat(returnedFaculty.getId()).isEqualTo(facultyId);
        assertThat(returnedFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(returnedFaculty.getColor()).isEqualTo("Белый");

        //проверка состояния базы данных
        Optional<Faculty> foundFaculty = facultyRepository.findById(returnedFaculty.getId());
        assertThat(foundFaculty).isPresent();
        Faculty dbFaculty = foundFaculty.get();
        assertThat(dbFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(dbFaculty.getColor()).isEqualTo("Белый");

    }

    @Test
    public void getFacultyById_shouldReturn404_whenFacultyNotFound() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Белый");
        facultyRepository.save(faculty);
        Long facultyId = faculty.getId();
        Long nonExistentId = -1L;


        //Act
        String url = "http://localhost:" + port + "/faculty/{id}";
        ResponseEntity<Faculty> response = testRestTemplate.getForEntity(url, Faculty.class, nonExistentId);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(1);
        assertThat(facultyRepository.existsById(nonExistentId)).isFalse();
        Optional<Faculty> existingFaculty = facultyRepository.findById(faculty.getId());
        assertThat(existingFaculty).isPresent();
        Faculty dbFaculty = existingFaculty.get();
        assertThat(dbFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(dbFaculty.getColor()).isEqualTo("Белый");

    }


    @Test
    public void updateFacultyById_whenEnterIdAndNewData_theModifiedFacultyWithThatIdIsReturned() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Серый");
        facultyRepository.save(faculty);
        Long idFaculty = faculty.getId();
        Faculty modifiedFaculty = new Faculty();
        modifiedFaculty.setName("Строительный факультет");
        modifiedFaculty.setColor("Синий");


        //Act
        String url = "http://localhost:" + port + "/faculty/{id}";
        ResponseEntity<Faculty> response = testRestTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(modifiedFaculty), Faculty.class, idFaculty);

        //Assert
        //проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Faculty returnedFaculty = response.getBody();
        assertThat(returnedFaculty).isNotNull();
        assertThat(returnedFaculty.getId()).isEqualTo(idFaculty);
        assertThat(returnedFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(returnedFaculty.getColor()).isEqualTo("Синий");

        //проверка состояния базы данных
        Optional<Faculty> savedFaculty = facultyRepository.findById(returnedFaculty.getId());
        Faculty foundFaculty = savedFaculty.orElseThrow(() -> new AssertionError("Факультет не найден в базе данных"));
        assertThat(foundFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(foundFaculty.getColor()).isEqualTo("Синий");

    }

    @Test
    public void updateFacultyById_shouldReturn404_whenFacultyNotFound() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Белый");
        facultyRepository.save(faculty);
        Long nonExistentId = -1L;

        Faculty dummyFaculty = new Faculty();
        dummyFaculty.setName("Dummy");
        dummyFaculty.setColor("Белый");


        //Act
        String url = "http://localhost:" + port + "/faculty/{id}";
        HttpEntity<Faculty> requestEntity = new HttpEntity<>(dummyFaculty);
        ResponseEntity<Faculty> response = testRestTemplate.exchange(url, HttpMethod.PUT, requestEntity, Faculty.class, nonExistentId);


        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(1);
        assertThat(facultyRepository.existsById(nonExistentId)).isFalse();
        Optional<Faculty> existingFaculty = facultyRepository.findById(faculty.getId());
        assertThat(existingFaculty).isPresent();
        Faculty dbFaculty = existingFaculty.get();
        assertThat(dbFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(dbFaculty.getColor()).isEqualTo("Белый");

    }

    @Test
    public void updateFacultyById__shouldReturn400_whenFacultyNameIsBlank() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Белый");
        facultyRepository.save(faculty);
        Long facultyId = faculty.getId();
        Faculty noNameFaculty = new Faculty();
        noNameFaculty.setName("");
        noNameFaculty.setColor("Синий");


        //Act
        String url = "http://localhost:" + port + "/faculty/{id}";
        HttpEntity<Faculty> requestEntity = new HttpEntity<>(noNameFaculty);
        ResponseEntity<Faculty> response = testRestTemplate.exchange(url, HttpMethod.PUT, requestEntity, Faculty.class, facultyId);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(1);
        Optional<Faculty> existingFaculty = facultyRepository.findById(faculty.getId());
        assertThat(existingFaculty).isPresent();
        Faculty dbFaculty = existingFaculty.get();
        assertThat(dbFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(dbFaculty.getColor()).isEqualTo("Белый");

    }

    @Test
    public void updateFacultyById__shouldReturn400_whenFacultyColorIsBlank() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Белый");
        facultyRepository.save(faculty);
        Long facultyId = faculty.getId();
        Faculty noColorFaculty = new Faculty();
        noColorFaculty.setName("Архитектурный");
        noColorFaculty.setColor("");


        //Act
        String url = "http://localhost:" + port + "/faculty/{id}";
        HttpEntity<Faculty> requestEntity = new HttpEntity<>(noColorFaculty);
        ResponseEntity<Faculty> response = testRestTemplate.exchange(url, HttpMethod.PUT, requestEntity, Faculty.class, facultyId);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(1);
        Optional<Faculty> existingFaculty = facultyRepository.findById(faculty.getId());
        assertThat(existingFaculty).isPresent();
        Faculty dbFaculty = existingFaculty.get();
        assertThat(dbFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(dbFaculty.getColor()).isEqualTo("Белый");

    }

    @Test
    public void deleteFacultyById_whenEnterTheIdFaculty_theFacultyIsRemovedFromTheDatabaseWithThatId() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Белый");
        facultyRepository.save(faculty);
        Long idFaculty = faculty.getId();

        //Act
        String url = "http://localhost:" + port + "/faculty/{id}";
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class, idFaculty);

        //Assert
        //проверка Http - ответа
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        assertThat(deleteResponse.getBody()).isNull();

        //проверка состояния базы данных
        Optional<Faculty> foundFaculty = facultyRepository.findById(idFaculty);
        assertThat(foundFaculty).isEmpty();

    }

    @Test
    public void deleteFacultyById_shouldReturn404_whenFacultyNotFound() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Белый");
        facultyRepository.save(faculty);
        Long nonExistentId = -1L;


        //Act
        String url = "http://localhost:" + port + "/faculty/{id}";
        ResponseEntity<Void> response = testRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class, nonExistentId);


        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(1);
        assertThat(facultyRepository.existsById(nonExistentId)).isFalse();
        Optional<Faculty> existingFaculty = facultyRepository.findById(faculty.getId());
        assertThat(existingFaculty).isPresent();
        Faculty dbFaculty = existingFaculty.get();
        assertThat(dbFaculty.getName()).isEqualTo("Строительный факультет");
        assertThat(dbFaculty.getColor()).isEqualTo("Белый");

    }


    @Test
    public void shouldReturnFilteredFaculties_whenSearchParamProvided() {
        //Arrange
        Faculty facultyOne = new Faculty();
        facultyOne.setName("Факультет 1");
        facultyOne.setColor("Белый");
        facultyRepository.save(facultyOne);
        Long idFacultyOne = facultyOne.getId();


        Faculty facultyTwo = new Faculty();
        facultyTwo.setName("Факультет 2");
        facultyTwo.setColor("Синий");
        facultyRepository.save(facultyTwo);

        Faculty facultyThree = new Faculty();
        facultyThree.setName("Факультет 3");
        facultyThree.setColor("Красный");
        facultyRepository.save(facultyThree);


        //Act
        String url = "http://localhost:" + port + "/faculty?search={search}";
        String search = "Белый";
        ResponseEntity<Faculty[]> response = testRestTemplate.getForEntity(url, Faculty[].class, search);

        //Assert
        //проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Faculty[] returnedFaculty = response.getBody();
        assertThat(returnedFaculty).isNotNull();
        assertThat(returnedFaculty).hasSize(1);

        Faculty foundFaculty = returnedFaculty[0];
        assertThat(foundFaculty.getId()).isEqualTo(idFacultyOne);
        assertThat(foundFaculty.getName()).isEqualTo("Факультет 1");
        assertThat(foundFaculty.getColor()).isEqualTo("Белый");

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(3);


    }

    @Test
    public void shouldReturnAllFaculties_whenNoSearchParam() {
        //Arrange
        Faculty facultyOne = new Faculty();
        facultyOne.setName("Факультет 1");
        facultyOne.setColor("Фиолетовый");
        facultyRepository.save(facultyOne);
        Long idFacultyOne = facultyOne.getId();

        Faculty facultyTwo = new Faculty();
        facultyTwo.setName("Факультет 2");
        facultyTwo.setColor("Синий");
        facultyRepository.save(facultyTwo);
        Long idFacultyTwo = facultyTwo.getId();

        Faculty facultyThree = new Faculty();
        facultyThree.setName("Факультет 3");
        facultyThree.setColor("Красный");
        facultyRepository.save(facultyThree);
        Long idFacultyThree = facultyThree.getId();

        //Act
        String url = "http://localhost:" + port + "/faculty";
        ResponseEntity<Faculty[]> response = testRestTemplate.getForEntity(url, Faculty[].class);

        //Assert
        //проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


        //проверка тела ответа
        Faculty[] returnedFaculties = response.getBody();
        assertThat(returnedFaculties).isNotNull();
        assertThat(returnedFaculties).hasSize(3);
        assertThat(returnedFaculties).extracting(Faculty::getId).containsExactlyInAnyOrder(idFacultyOne, idFacultyTwo, idFacultyThree);


        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(3);

    }

    @Test
    public void getFaculties_shouldReturnEmptyList_whenSearchParametersFacultyNotFound() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Строительный факультет");
        faculty.setColor("Белый");
        facultyRepository.save(faculty);
        Long facultyId = faculty.getId();

        //Act
        String url = "http://localhost:" + port + "/faculty/color/{color}";
        String color = "Желтый";
        ResponseEntity<Faculty[]> response = testRestTemplate.getForEntity(url, Faculty[].class, color);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Faculty[] returnedResponse = response.getBody();
        assertThat(returnedResponse).isNotNull();
        assertThat(returnedResponse).isEmpty();

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(1);

    }


    @Test
    public void getFacultyByColor_enterTheColorFaculty_shouldReturnFilteredFacultiesByColor() {
        //Arrange
        Faculty facultyOne = new Faculty();
        facultyOne.setName("Факультет 1");
        facultyOne.setColor("Черный");
        facultyRepository.save(facultyOne);
        Long idFacultyOne = facultyOne.getId();

        Faculty facultyTwo = new Faculty();
        facultyTwo.setName("Факультет 2");
        facultyTwo.setColor("Синий");
        facultyRepository.save(facultyTwo);

        Faculty facultyThree = new Faculty();
        facultyThree.setName("Факультет 3");
        facultyThree.setColor("Красный");
        facultyRepository.save(facultyThree);

        //Act
        String url = "http://localhost:" + port + "/faculty/color/{color}";
        String color = "Черный";
        ResponseEntity<Faculty[]> response = testRestTemplate.getForEntity(url, Faculty[].class, color);

        //Assert
        //проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Faculty[] returnedFaculty = response.getBody();
        assertThat(returnedFaculty).isNotNull();
        assertThat(returnedFaculty).hasSize(1);

        Faculty returnedFacultyByColor = returnedFaculty[0];
        assertThat(returnedFacultyByColor.getId()).isEqualTo(idFacultyOne);
        assertThat(returnedFacultyByColor.getName()).isEqualTo("Факультет 1");
        assertThat(returnedFacultyByColor.getColor()).isEqualTo("Черный");

        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(3);
        Optional<Faculty> foundFaculty = facultyRepository.findById(returnedFacultyByColor.getId());
        assertThat(foundFaculty).isPresent();
        Faculty dbFaculty = foundFaculty.get();
        assertThat(dbFaculty.getName()).isEqualTo("Факультет 1");
        assertThat(dbFaculty.getColor()).isEqualTo("Черный");

    }

    @Test
    public void getFacultyByColor_shouldReturnEmptyList_whenFacultyByColorNotFound() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Факультет 1");
        faculty.setColor("Черный");
        facultyRepository.save(faculty);
        Long idFaculty = faculty.getId();


        //Act
        String url = "http://localhost:" + port + "/faculty/color/{color}";
        String nonExistent = "Белый";
        ResponseEntity<Faculty[]> response = testRestTemplate.getForEntity(url, Faculty[].class, nonExistent);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();


        //проверка состояния базы данных
        assertThat(facultyRepository.findAll()).hasSize(1);

    }


    @Test
    public void enterTheIdFaculty_shouldReturnTheFacultyStudents() {
        //Arrange
        Faculty faculty = new Faculty();
        faculty.setName("Факультет 1");
        faculty.setColor("Черный");
        facultyRepository.save(faculty);
        Long facultyId = faculty.getId();

        Student studentOne = new Student();
        studentOne.setName("Max");
        studentOne.setAge(15);
        studentOne.setFaculty(faculty);
        studentRepository.save(studentOne);
        Long idStudentOne = studentOne.getId();

        Student studentTwo = new Student();
        studentTwo.setName("Olga");
        studentTwo.setAge(25);
        studentTwo.setFaculty(faculty);
        studentRepository.save(studentTwo);
        Long idStudentTwo = studentTwo.getId();


        //Act
        String url = "http://localhost:" + port + "/faculty/{facultyId}/students";
        ResponseEntity<Student[]> response = testRestTemplate.getForEntity(url, Student[].class, facultyId);

        //Assert
        //проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //проверка тела ответа
        Student[] returnedStudents = response.getBody();
        assertThat(returnedStudents).isNotNull();
        assertThat(returnedStudents).hasSize(2);
        assertThat(returnedStudents).extracting(Student::getId).containsExactlyInAnyOrder(idStudentOne, idStudentTwo);

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(2);

    }

    @Test
    public void getStudentsByFacultyId_shouldReturnEmptyList_whenStudentsNotFound() {
        //Arrange
        Faculty facultyOne = new Faculty();
        facultyOne.setName("Факультет 1");
        facultyOne.setColor("Черный");
        facultyRepository.save(facultyOne);
        Long idFacultyOne = facultyOne.getId();

        Faculty facultyTwo = new Faculty();
        facultyTwo.setName("Факультет 2");
        facultyTwo.setColor("Синий");
        facultyRepository.save(facultyTwo);

        Student student = new Student();
        student.setName("Max");
        student.setAge(15);
        student.setFaculty(facultyTwo);
        studentRepository.save(student);
        Long idStudent = student.getId();


        //Act
        String url = "http://localhost:" + port + "/faculty/{facultyId}/students";
        ResponseEntity<Student[]> response = testRestTemplate.getForEntity(url, Student[].class, idFacultyOne);

        //Assert
        // проверка Http - ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();

        //проверка состояния базы данных
        assertThat(studentRepository.findAll()).hasSize(1);


    }

}
