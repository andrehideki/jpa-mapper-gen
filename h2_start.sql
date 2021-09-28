
CREATE TABLE person (
	name VARCHAR(255) PRIMARY KEY NOT NULL,
	age INT NOT NULL
);

CREATE TABLE job (
	name VARCHAR(255) PRIMARY KEY NOT NULL,
	salary DOUBLE NOT NULL
);

CREATE TABLE person_job (
	person_name VARCHAR(255) PRIMARY KEY NOT NULL,
	job_name VARCHAR(255) PRIMARY KEY NOT NULL,
	salary DOUBLE NOT NULL
);