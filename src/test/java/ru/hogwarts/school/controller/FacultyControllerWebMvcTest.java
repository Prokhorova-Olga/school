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
import ru.hogwarts.school.service.FacultyService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FacultyController.class)
public class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FacultyService facultyService;

    @Test
    public void createFaculty_shouldReturnCreatedFacultyWithId() throws Exception {
        //Arrange
        Long id = 1L;
        String name = "Факультет";
        String color = "Белый";

        Faculty createdFaculty = new Faculty();
        createdFaculty.setId(id);
        createdFaculty.setName(name);
        createdFaculty.setColor(color);

        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(createdFaculty);

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", name);
        requestBody.put("color", color);

        //Act && Assert
        mockMvc.perform(post("/faculty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.color").value(color));

        // Дополнительно: проверяем, что в сервис передан объект без id
        ArgumentCaptor<Faculty> captor = ArgumentCaptor.forClass(Faculty.class);
        verify(facultyService).createFaculty(captor.capture());
        Faculty captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo(name);
        assertThat(captured.getColor()).isEqualTo(color);
        assertThat(captured.getId()).isNull();

    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    public void createFaculty_shouldReturn400_whenFacultyNameIsBlank(String invalidName) throws Exception {
        //Arrange
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", invalidName);
        requestBody.put("color", "Белый");

        //Act && Assert
        mockMvc.perform(post("/faculty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isBadRequest());

        // Проверяем, что сервис не вызывался
        verify(facultyService, never()).createFaculty(any());
    }

    @Test
    public void createFaculty_shouldReturn400_whenFacultyColorIsBlank() throws Exception {
        //Arrange
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Факультет");
        requestBody.put("color", "");

        //Act && Assert
        mockMvc.perform(post("/faculty")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isBadRequest());

        // Проверяем, что сервис не вызывался
        verify(facultyService, never()).createFaculty(any());

    }

    @Test
    public void getFacultyById_shouldReturnFacultyWithId() throws Exception {
        //Arrange
        Long id = 1L;

        Faculty createdFaculty = new Faculty();
        createdFaculty.setId(id);
        createdFaculty.setName("Факультет");
        createdFaculty.setColor("Белый");

        when(facultyService.getFacultyInfo(eq(id))).thenReturn(Optional.of(createdFaculty));


        //Act && Assert
        mockMvc.perform(get("/faculty/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Факультет"))
                .andExpect(jsonPath("$.color").value("Белый"));

        // Проверяем, что сервис был вызван с переданным ID
        verify(facultyService).getFacultyInfo(eq(id));

    }

    @Test
    public void getFacultyById_shouldReturn404_whenFacultyNotFound() throws Exception {
        //Arrange
        Long invalidId = -1L;

        when(facultyService.getFacultyInfo(invalidId)).thenReturn(Optional.empty());

        //Act && Assert
        mockMvc.perform(get("/faculty/{id}", invalidId))
                .andExpect(status().isNotFound());

        // Проверяем, что сервис был вызван с переданным ID
        verify(facultyService).getFacultyInfo(eq(invalidId));

    }

    @Test
    public void updateFacultyById_whenEnterIdAndNewData_theModifiedFacultyWithThatIdIsReturned() throws Exception {
        //Arrange
        Long id = 1L;
        String originalName = "Факультет";
        String originalColor = "Белый";
        String newColor = "Зеленый";

        Faculty updatedFaculty = new Faculty();
        updatedFaculty.setId(id);
        updatedFaculty.setName(originalName);
        updatedFaculty.setColor(newColor);

        when(facultyService.updateFaculty(eq(id), any(Faculty.class))).thenReturn(updatedFaculty);

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", originalName);
        requestBody.put("color", newColor);

        //Act && Assert
        mockMvc.perform(put("/faculty/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(originalName))
                .andExpect(jsonPath("$.color").value(newColor));

        ArgumentCaptor<Faculty> captor = ArgumentCaptor.forClass(Faculty.class);
        verify(facultyService).updateFaculty(eq(id), captor.capture());
        Faculty captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo(originalName);
        assertThat(captured.getColor()).isEqualTo(newColor);
        assertThat(captured.getId()).isNull();
    }

    @Test
    public void updateFacultyById_shouldReturn404_whenFacultyNotFound() throws Exception {
        //Arrange
        Long id = 1L;

        when(facultyService.updateFaculty(eq(id), any(Faculty.class))).thenReturn(null);

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Несуществующий");
        requestBody.put("color", "Красный");

        //Act && Assert
        mockMvc.perform(put("/faculty/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isNotFound());

        //Проверяем, что сервис был вызван с переданным ID
        verify(facultyService).updateFaculty(eq(id), any(Faculty.class));

    }

    @Test
    public void updateFacultyById_shouldReturn400_whenFacultyNameIsBlank() throws Exception {
        //Arrange
        Long id = 1L;

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "");
        requestBody.put("color", "Белый");

        //Act && Assert
        mockMvc.perform(put("/faculty/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isBadRequest());

        // Проверяем, что сервис не вызывался
        verify(facultyService, never()).updateFaculty(eq(id), any(Faculty.class));

    }

    @Test
    public void updateFacultyById_shouldReturn400_whenFacultyColorIsBlank() throws Exception {
        //Arrange
        Long id = 1L;

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Факультет");
        requestBody.put("color", "");

        //Act && Assert
        mockMvc.perform(put("/faculty/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toString()))
                .andExpect(status().isBadRequest());

        // Проверяем, что сервис не вызывался
        verify(facultyService, never()).updateFaculty(eq(id), any(Faculty.class));

    }

    @Test
    public void deleteFacultyById_whenEnterTheIdFaculty_theFacultyIsRemovedFromTheDatabaseWithThatId() throws Exception {
        //Arrange
        Long id = 1L;

        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName("Факультет");
        faculty.setColor("Белый");

        when(facultyService.getFacultyInfo(eq(id))).thenReturn(Optional.of(faculty));
        doNothing().when(facultyService).deleteFaculty(eq(id));

        //Act && Assert
        mockMvc.perform(delete("/faculty/{id}", id))
                .andExpect(status().isOk());

        // Проверяем, что сервисы были вызваны с переданным ID
        verify(facultyService).getFacultyInfo(eq(id));
        verify(facultyService).deleteFaculty(eq(id));

    }

    @Test
    public void deleteFacultyById_shouldReturn404_whenFacultyNotFound() throws Exception {
        //Arrange
        Long id = 1L;

        when(facultyService.getFacultyInfo(id)).thenReturn(Optional.empty());

        //Act && Assert
        mockMvc.perform(delete("/faculty/{id}", id))
                .andExpect(status().isNotFound());

        // Проверяем, что сервис был вызван с переданным ID
        verify(facultyService).getFacultyInfo(eq(id));
        // Проверяем, что сервис не вызывался
        verify(facultyService, never()).deleteFaculty(eq(id));

    }

    @Test
    public void getFaculties_shouldReturnFilteredFaculties_whenSearchParamProvided() throws Exception {
        //Arrange
        String search = "Белый";

        Faculty facultyOne = new Faculty();
        facultyOne.setId(1L);
        facultyOne.setName("Факультет 1");
        facultyOne.setColor("Белый");

        Faculty facultyTwo = new Faculty();
        facultyTwo.setId(2L);
        facultyTwo.setName("Факультет 2");
        facultyTwo.setColor("Синий");

        Faculty facultyThree = new Faculty();
        facultyThree.setId(3L);
        facultyThree.setName("Белый факультет");
        facultyThree.setColor("Красный");

        List<Faculty> filtered = Arrays.asList(facultyOne, facultyThree);

        when(facultyService.getFacultyByNameOrColor(search)).thenReturn(filtered);

        //Act && Assert
        mockMvc.perform(get("/faculty?search={search}", search))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Факультет 1", "Белый факультет")))
                .andExpect(jsonPath("$[*].color", containsInAnyOrder("Белый", "Красный")));


        // Проверяем, что сервис вызван с правильной строкой поиска
        verify(facultyService).getFacultyByNameOrColor(search);
        // И что метод getAllFaculties не вызывался
        verify(facultyService, never()).getAllFaculties();

    }

    @Test
    public void getFaculties_shouldReturnAllFaculties_whenNoSearchParam() throws Exception {
        //Arrange
        Faculty facultyOne = new Faculty();
        facultyOne.setId(1L);
        facultyOne.setName("Факультет 1");
        facultyOne.setColor("Белый");

        Faculty facultyTwo = new Faculty();
        facultyTwo.setId(2L);
        facultyTwo.setName("Факультет 2");
        facultyTwo.setColor("Синий");

        Faculty facultyThree = new Faculty();
        facultyThree.setId(3L);
        facultyThree.setName("Белый факультет");
        facultyThree.setColor("Красный");

        List<Faculty> allFaculties = Arrays.asList(facultyOne, facultyTwo, facultyThree);

        when(facultyService.getAllFaculties()).thenReturn(allFaculties);

        //Act && Assert
        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1, 2, 3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Факультет 1", "Факультет 2", "Белый факультет")))
                .andExpect(jsonPath("$[*].color", containsInAnyOrder("Белый", "Синий", "Красный")));


        // Проверяем, что сервис был вызван
        verify(facultyService).getAllFaculties();
        //И что метод getFacultyByNameOrColor() не вызывался
        verify(facultyService, never()).getFacultyByNameOrColor(anyString());
    }

    @Test
    public void getFaculties_shouldReturnEmptyList_whenSearchParametersFacultyNotFound() throws Exception {
        //Arrange
        String searchColor = "Белый";

        List<Faculty> emptyList = new ArrayList<>();

        when(facultyService.getFacultyByNameOrColor(searchColor)).thenReturn(emptyList);

        //Act && Assert
        mockMvc.perform(get("/faculty?search={search}", searchColor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Проверяем, что сервис вызван со строкой поиска
        verify(facultyService).getFacultyByNameOrColor(searchColor);
        // И что метод getAllFaculties не вызывался
        verify(facultyService, never()).getAllFaculties();
    }

    @Test
    public void getFacultyByColor_enterTheColorFaculty_shouldReturnFilteredFacultiesByColor() throws Exception {
        //Arrange
        String searchColor = "Белый";

        Faculty facultyOne = new Faculty();
        facultyOne.setId(1L);
        facultyOne.setName("Факультет 1");
        facultyOne.setColor("Белый");

        Faculty facultyTwo = new Faculty();
        facultyTwo.setId(2L);
        facultyTwo.setName("Факультет 2");
        facultyTwo.setColor("Синий");

        Faculty facultyThree = new Faculty();
        facultyThree.setId(3L);
        facultyThree.setName("Белый факультет");
        facultyThree.setColor("Красный");

        List<Faculty> filtered = List.of(facultyOne);

        when(facultyService.getFacultyByColor(searchColor)).thenReturn(filtered);


        //Act && Assert
        mockMvc.perform(get("/faculty/color/{color}", searchColor))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(1)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Факультет 1")))
                .andExpect(jsonPath("$[*].color", containsInAnyOrder("Белый")));


        // Проверяем, что сервис был вызван
        verify(facultyService).getFacultyByColor(searchColor);
        // И что метод getAllFaculties не вызывался
        verify(facultyService, never()).getAllFaculties();

    }

    @Test
    public void getFacultyByColor_shouldReturnEmptyList_whenFacultyByColorNotFound() throws Exception {
        //Arrange
        String nonExistentColor = "Белый";

        List<Faculty> emptyList = new ArrayList<>();

        when(facultyService.getFacultyByColor(nonExistentColor)).thenReturn(emptyList);

        //Act && Assert
        mockMvc.perform(get("/faculty/color/{color}", nonExistentColor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Проверяем, что сервис вызван со строкой поиска
        verify(facultyService).getFacultyByColor(nonExistentColor);
        // И что метод getAllFaculties не вызывался
        verify(facultyService, never()).getAllFaculties();
    }

    @Test
    public void getStudentsByFacultyId_enterTheIdFaculty_shouldReturnTheFacultyStudents() throws Exception {
        //Arrange
        Long facultyId = 1L;

        Faculty faculty = new Faculty();
        faculty.setId(facultyId);
        faculty.setName("Факультет");
        faculty.setColor("Белый");

        Student studentOne = new Student();
        studentOne.setId(11L);
        studentOne.setName("Ожидаемый студент");
        studentOne.setAge(15);
        studentOne.setFaculty(faculty);

        Student studentTwo = new Student();
        studentTwo.setId(22L);
        studentTwo.setName("Просто студент");
        studentTwo.setAge(16);

        List<Student> filtered = List.of(studentOne);

        when(facultyService.getStudentsByFacultyId(facultyId)).thenReturn(filtered);

        //Act && Assert
        mockMvc.perform(get("/faculty/{facultyId}/students", facultyId))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(11)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Ожидаемый студент")))
                .andExpect(jsonPath("$[*].age", containsInAnyOrder(15)));

        // Проверяем, что сервис был вызван
        verify(facultyService).getStudentsByFacultyId(facultyId);
        // И что метод getAllFaculties не вызывался
        verify(facultyService, never()).getAllFaculties();

    }

    @Test
    public void getStudentsByFacultyId_shouldReturnEmptyList_whenStudentsNotFound() throws Exception {
        //Arrange
        Long facultyId = 1L;

        Faculty faculty = new Faculty();
        faculty.setId(facultyId);
        faculty.setName("Факультет");
        faculty.setColor("Белый");

        List<Student> emptyList = new ArrayList<>();

        when(facultyService.getStudentsByFacultyId(facultyId)).thenReturn(emptyList);

        //Act && Assert
        mockMvc.perform(get("/faculty/{facultyId}/students", facultyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Проверяем, что сервис был вызван
        verify(facultyService).getStudentsByFacultyId(facultyId);
        // И что метод getAllFaculties не вызывался
        verify(facultyService, never()).getAllFaculties();

    }

}




















