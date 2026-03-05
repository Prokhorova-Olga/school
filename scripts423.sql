SELECT student.name, student.age, faculty.name AS faculty_name
FROM student
LEFT JOIN faculty
ON faculty.id = student.faculty_id;

SELECT student.id, student.name, student.age FROM student
INNER JOIN avatar
ON avatar.student_id = student.id;