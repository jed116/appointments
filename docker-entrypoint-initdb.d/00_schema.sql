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
    name TEXT NOT NULL UNIQUE
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
   description TEXT NOT NULL,
   FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
   UNIQUE (user_id)
);


CREATE TABLE tokens (
    userId BIGINT NOT NULL REFERENCES users UNIQUE,
    token TEXT PRIMARY KEY
);

CREATE TABLE appointment (
    id BIGSERIAL PRIMARY KEY,
    date_time TIMESTAMP NOT NULL,
    doctor_id BIGINT NOT NULL REFERENCES users,
    patient_id BIGINT REFERENCES users
);
