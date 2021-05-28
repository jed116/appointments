CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    login TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    secret TEXT NOT NULL,
    avatar TEXT NOT NULL DEFAULT 'noavatar.svg',
    removed BOOLEAN NOT NULL DEFAULT FALSE,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    activeDefault BOOLEAN NOT NULL DEFAULT FALSE,
    isAdmin     BOOLEAN NOT NULL DEFAULT FALSE,
    isChief     BOOLEAN NOT NULL DEFAULT FALSE,
    isDoctor    BOOLEAN NOT NULL DEFAULT FALSE,
    isPatient   BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE permissions (
    operation TEXT NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles,
    UNIQUE (operation, role_id)
);

CREATE TABLE user_roles(
    user_id BIGINT NOT NULL REFERENCES users,
    role_id BIGINT NOT NULL REFERENCES roles,
    active  BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, role_id)
);

CREATE TABLE user_info(
   user_id BIGINT NOT NULL REFERENCES users,
   firstName TEXT NOT NULL,
   secondName TEXT NOT NULL,
   description TEXT NOT NULL DEFAULT '',
   FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
   UNIQUE (user_id)
);


CREATE TABLE tokens (
    userId BIGINT NOT NULL REFERENCES users UNIQUE,
    token TEXT PRIMARY KEY
);

CREATE TABLE appointment (
    id BIGSERIAL PRIMARY KEY,
    dateTime TIMESTAMP NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,  -- 0=opened, 1=closed, -1=canceled

    doctor_id BIGINT NOT NULL REFERENCES users DEFAULT null,
    doctorFirstName TEXT NOT NULL DEFAULT '',
    doctorSecondName TEXT NOT NULL DEFAULT '',
    doctorDescription TEXT NOT NULL DEFAULT '',

    patient_id BIGINT REFERENCES users DEFAULT null,
    patientFirstName TEXT NOT NULL DEFAULT '',
    patientSecondName TEXT NOT NULL DEFAULT '',
    patientDescription TEXT NOT NULL DEFAULT '',

    accessCode TEXT NOT NULL DEFAULT '',
    result TEXT NOT NULL DEFAULT ''
);
