package ru.hogwarts.school.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
public class StudentControllerWebMvcTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @Test
    public void createStudent_shouldReturnCreatedStudentWithId() throws Exception {
        //Arrange
        Long id = 1L;
        String name = "Студент";
        int age = 15;

        Student createdStudent = new Student();
        createdStudent.setId(id);
        createdStudent.setName(name);
        createdStudent.setAge(age);

        when(studentService.createStudent(any(Student.class))).thenReturn(createdStudent);

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", name);
        requestBody.put("age", age);

        //Act && Assert
        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.age").value(age));

        // Дополнительно: проверяем, что в сервис передан объект без id
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentService).createStudent(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(name);
        assertThat(captor.getValue().getAge()).isEqualTo(age);
        assertThat(captor.getValue().getId()).isNull();

    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    public void createStudent_shouldReturn400_whenStudentNameIsBlank(String invalidName) throws Exception {
        //Arrange
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", invalidName);
        requestBody.put("age", 15);

        //Act && Assert
        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isBadRequest());

        // Проверяем, что сервис не вызывался
        verify(studentService, never()).createStudent(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 155})
    public void createStudent_shouldReturn400_whenAgeIsParametersIncorrect(int invalidAge) throws Exception {
        //Arrange
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Студент");
        requestBody.put("age", invalidAge);

        //Act && Assert
        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isBadRequest());

        // Проверяем, что сервис не вызывался
        verify(studentService, never()).createStudent(any());

    }

    @Test
    public void getStudentInfo_enterTheIdStudent_shouldReturnStudentWithId() throws Exception {
        //Arrange
        Long id = 1L;

        Student student = new Student();
        student.setId(id);
        student.setName("Студент");
        student.setAge(15);

        when(studentService.getStudentInfo(eq(id))).thenReturn(Optional.of(student));

        //Act && Assert
        mockMvc.perform(get("/student/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Студент"))
                .andExpect(jsonPath("$.age").value(15));

        // Проверяем, что сервис был вызван с переданным ID
        verify(studentService).getStudentInfo(eq(id));

    }

    @Test
    public void getStudentById_shouldReturn404_whenStudentNotFound() throws Exception {
        //Arrange
        Long id = -1L;

        when(studentService.getStudentInfo(eq(id))).thenReturn(Optional.empty());

        //Act && Assert
        mockMvc.perform(get("/student/{id}", id))
                .andExpect(status().isNotFound());

        // Проверяем, что сервис был вызван с переданным ID
        verify(studentService).getStudentInfo(eq(id));

    }

    @Test
    public void updateStudentById_whenEnterIdAndNewData_theModifiedStudentWithThatIdIsReturned() throws Exception {
        //Arrange
        Long id = 1L;
        String originalName = "Студент";
        int originalAge = 15;
        String newName = "Измененный студент";

        Student updateStudent = new Student();
        updateStudent.setId(id);
        updateStudent.setName(newName);
        updateStudent.setAge(originalAge);

        when(studentService.updateStudent(eq(id), any(Student.class))).thenReturn(updateStudent);

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", newName);
        requestBody.put("age", originalAge);

        //Act && Assert
        mockMvc.perform(put("/student/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.age").value(originalAge));

        // Дополнительно: проверяем, что в сервис передан объект без id
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentService).updateStudent(eq(id), captor.capture());
        Student captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo(newName);
        assertThat(captured.getAge()).isEqualTo(originalAge);
        assertThat(captured.getId()).isNull();
    }

    @Test
    public void updateStudentById_shouldReturn404_whenStudentNotFound() throws Exception {
        //Arrange
        Long id = 1L;

        when(studentService.updateStudent(eq(id), any(Student.class))).thenReturn(null);

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Несуществующий студент");
        requestBody.put("age", 15);


        //Act && Assert
        mockMvc.perform(put("/student/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isNotFound());


        // Проверяем, что сервис был вызван с переданным ID
        verify(studentService).updateStudent(eq(id), any(Student.class));

    }

    @Test
    public void updateStudentById_shouldReturn400_whenStudentNameIsBlank() throws Exception {
        //Arrange
        Long id = 1L;

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "");
        requestBody.put("age", 15);

        //Act && Assert
        mockMvc.perform(put("/student/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isBadRequest());

        // Проверяем, что сервис не вызывался
        verify(studentService, never()).updateStudent(eq(id), any(Student.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 151})
    public void updateStudentById_shouldReturn400_whenAgeIsParametersIncorrect(int invalidAge) throws Exception {
        //Arrange
        Long id = 1L;

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Студент");
        requestBody.put("age", invalidAge);

        //Act && Assert
        mockMvc.perform(put("/student/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isBadRequest());

        // Проверяем, что сервис не вызывался
        verify(studentService, never()).updateStudent(eq(id), any(Student.class));
    }

    @Test
    public void deleteStudentById_whenEnterTheIdStudent_theStudentIsRemovedFromTheDatabaseWithThatId() throws Exception {
        //Arrange
        Long id = 1L;

        Student student = new Student();
        student.setId(id);
        student.setName("Студент");
        student.setAge(15);

        when(studentService.getStudentInfo(eq(id))).thenReturn(Optional.of(student));
        doNothing().when(studentService).deleteStudent(eq(id));

        //Act && Assert
        mockMvc.perform(delete("/student/{id}", id))
                .andExpect(status().isOk());

        // Проверяем, что сервисы были вызваны с переданным ID
        verify(studentService).getStudentInfo(eq(id));
        verify(studentService).deleteStudent(eq(id));

    }

    @Test
    public void deleteStudentById_shouldReturn404_whenStudentNotFound() throws Exception {
        //Arrange
        Long id = 1L;

        Student student = new Student();
        student.setId(id);
        student.setName("Студент");
        student.setAge(15);

        when(studentService.getStudentInfo(eq(id))).thenReturn(Optional.empty());


        //Act && Assert
        mockMvc.perform(delete("/student/{id}", id))
                .andExpect(status().isNotFound());


        // Проверяем, что сервис был вызван с переданным ID
        verify(studentService).getStudentInfo(eq(id));
        // Проверяем, что сервис не вызывался
        verify(studentService, never()).deleteStudent(eq(id));

    }

    @Test
    public void getStudentsByAge_enterTheAgeStudent_shouldReturnFilteredStudentsByAge() throws Exception {
        //Arrange
        int searchAge = 15;

        Student studentOne = new Student();
        studentOne.setId(1L);
        studentOne.setName("Студент 1");
        studentOne.setAge(15);

        Student studentTwo = new Student();
        studentTwo.setId(2L);
        studentTwo.setName("Студент 2");
        studentTwo.setAge(10);

        List<Student> filtered = List.of(studentOne);

        when(studentService.getStudentByAge(searchAge)).thenReturn(filtered);

        //Act && Assert
        mockMvc.perform(get("/student/age/{age}", searchAge))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Студент 1")))
                .andExpect(jsonPath("$[*].age", containsInAnyOrder(15)));

        // Проверяем, что сервис был вызван
        verify(studentService).getStudentByAge(searchAge);
        // Проверяем, что сервис не вызывался
        verify(studentService, never()).getAllStudents();

    }

    @Test
    public void getStudentsByAge_shouldReturnEmptyList_whenNoStudentWithSuchAge() throws Exception {
        //Arrange
        int searchAge = 15;

        List<Student> emptyList = new ArrayList<>();

        when(studentService.getStudentByAge(searchAge)).thenReturn(emptyList);

        //Act && Assert
        mockMvc.perform(get("/student/age/{age}", searchAge))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Проверяем, что сервис был вызван
        verify(studentService).getStudentByAge(searchAge);
        // Проверяем, что сервис не вызывался
        verify(studentService, never()).getAllStudents();

    }

    @Test
    public void shouldReturnFilteredStudents_whenSearchMinAndMaxAgeParam() throws Exception {
        //Arrange
        int minAge = 15;
        int maxAge = 17;

        Student studentOne = new Student();
        studentOne.setId(1L);
        studentOne.setName("Студент 1");
        studentOne.setAge(15);

        Student studentTwo = new Student();
        studentTwo.setId(2L);
        studentTwo.setName("Студент 2");
        studentTwo.setAge(16);

        Student studentThree = new Student();
        studentThree.setId(3L);
        studentThree.setName("Студент 3");
        studentThree.setAge(20);

        List<Student> filtered = Arrays.asList(studentOne, studentTwo);

        when(studentService.findStudentsByAgeBetweenTheParameters(minAge, maxAge)).thenReturn(filtered);

        //Act && Assert
        mockMvc.perform(get("/student/age/between?min={min}&max={max}", minAge, maxAge))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Студент 1", "Студент 2")))
                .andExpect(jsonPath("$[*].age", containsInAnyOrder(15, 16)));


        // Проверяем, что сервис был вызван с правильными параметрами поиска
        verify(studentService).findStudentsByAgeBetweenTheParameters(minAge, maxAge);
        // Проверяем, что сервис не вызывался
        verify(studentService, never()).getAllStudents();

    }

    @Test
    public void shouldReturn200andEmptyList_whenAgeIsParametersIncorrect() throws Exception {
        //Arrange
        int min = 20;
        int max = 15;

        List<Student> emptyList = new ArrayList<>();

        when(studentService.findStudentsByAgeBetweenTheParameters(min, max)).thenReturn(emptyList);

        //Act && Assert
        mockMvc.perform(get("/student/age/between?min={min}&max={max}", min, max))
                .andExpect(status().isOk());

        // Проверяем, что сервис был вызван с правильными параметрами поиска
        verify(studentService).findStudentsByAgeBetweenTheParameters(min, max);
        // Проверяем, что сервис не вызывался
        verify(studentService, never()).getAllStudents();

    }

    @Test
    public void getFacultyByStudentId_enterTheIdStudent_shouldReturnTheStudentFaculty() throws Exception {
        //Arrange
        Long idStudent = 1L;

        Faculty facultyOne = new Faculty();
        facultyOne.setId(11L);
        facultyOne.setName("Ожидаемый факультет");
        facultyOne.setColor("Белый");

        Faculty facultyTwo = new Faculty();
        facultyTwo.setId(22L);
        facultyTwo.setName("Просто факультет");
        facultyTwo.setColor("Синий");

        Student student = new Student();
        student.setId(idStudent);
        student.setName("Студент");
        student.setAge(15);
        student.setFaculty(facultyOne);

        when(studentService.getFacultyByStudentId(eq(idStudent))).thenReturn(facultyOne);

        //Act && Assert
        mockMvc.perform(get("/student/{studentId}/faculty", idStudent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.name").value("Ожидаемый факультет"))
                .andExpect(jsonPath("$.color").value("Белый"));

        // Проверяем, что сервис был вызван с переданным ID
        verify(studentService).getFacultyByStudentId(eq(idStudent));

    }

    @Test
    public void getFacultyByStudentId_shouldReturn404_whenFacultyNotFound() throws Exception {
        //Arrange
        Long idStudent = 1L;

        when(studentService.getFacultyByStudentId(idStudent)).thenReturn(null);

        //Act && Assert
        mockMvc.perform(get("/student/{studentId}/faculty", idStudent))
                .andExpect(status().isNotFound());

        // Проверяем, что сервис был вызван с переданным ID
        verify(studentService).getFacultyByStudentId(eq(idStudent));

    }

}
