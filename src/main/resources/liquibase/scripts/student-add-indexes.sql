-- liquibase formatted sql

-- changeset olga_prokhorova:1

CREATE INDEX idx_student_name ON student (name);