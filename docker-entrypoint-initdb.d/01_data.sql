INSERT INTO roles(name, activeDefault, isAdmin, isChief, isDoctor, isPatient)
VALUES ('ROLE_CHIEF', FALSE, FALSE, TRUE, FALSE, FALSE),
       ('ROLE_ADMIN', FALSE, TRUE, FALSE, FALSE, FALSE),
       ('ROLE_PATIENT', TRUE, FALSE, FALSE, FALSE, TRUE),
       ('ROLE_DOCTOR', FALSE, FALSE, FALSE, TRUE, FALSE);

------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

INSERT INTO permissions(operation, role_id)
SELECT '/api/users/roles', r.id FROM roles r WHERE r.name = 'ROLE_ADMIN';

------------------------------------------------------------------------------------------------------------------------

INSERT INTO permissions(operation, role_id)
SELECT '/api/user/roles/get',r.id FROM roles r WHERE r.name = 'ROLE_ADMIN';

INSERT INTO permissions(operation, role_id)
SELECT '/api/user/roles/append',r.id FROM roles r WHERE r.name = 'ROLE_ADMIN';

INSERT INTO permissions(operation, role_id)
SELECT '/api/user/roles/remove',r.id FROM roles r WHERE r.name = 'ROLE_ADMIN';

INSERT INTO permissions(operation, role_id)
SELECT '/api/user/roles/active',r.id FROM roles r WHERE r.name = 'ROLE_ADMIN';

------------------------------------------------------------------------------------------------------------------------

INSERT INTO permissions(operation, role_id)
SELECT '/api/users/find', r.id FROM roles r WHERE r.name = 'ROLE_ADMIN';

INSERT INTO permissions(operation, role_id)
SELECT '/api/users/find/doctors', r.id FROM roles r WHERE r.name IN ('ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_CHIEF');

INSERT INTO permissions(operation, role_id)
SELECT '/api/users/find/patients', r.id FROM roles r WHERE r.name IN ('ROLE_DOCTOR', 'ROLE_CHIEF');

INSERT INTO permissions(operation, role_id)
SELECT '/api/users/find/chiefs', r.id FROM roles r WHERE r.name IN ('ROLE_DOCTOR', 'ROLE_CHIEF');

INSERT INTO permissions(operation, role_id)
SELECT '/api/users/info/set', r.id FROM roles r WHERE r.name IN ('ROLE_ADMIN', 'ROLE_CHIEF', 'ROLE_DOCTOR', 'ROLE_PATIENT');

-------------------------------------------------------------------------------------------------------------appointment

INSERT INTO permissions(operation, role_id)
SELECT '/api/appointment/open', r.id FROM roles r WHERE r.name = 'ROLE_DOCTOR';

INSERT INTO permissions(operation, role_id)
SELECT '/api/appointment/book', r.id FROM roles r WHERE r.name = 'ROLE_PATIENT';


-- Different role functionality:
-- 1) Patient can unbook only own booked appointments 2) Chief can unbook any booked appointments
INSERT INTO permissions(operation, role_id)
SELECT '/api/appointment/unbook', r.id FROM roles r WHERE r.name IN ('ROLE_PATIENT', 'ROLE_CHIEF');

-- different role functionality
-- 1) Doctor can close only own appointments by access code 2) Chief can close any appointments without access code
INSERT INTO permissions(operation, role_id)
SELECT '/api/appointment/close', r.id FROM roles r WHERE r.name IN ('ROLE_DOCTOR', 'ROLE_CHIEF');

-- different role functionality
-- 1) Doctor can cancel only own unbooked appointments2) Chief can close any unbooked appointments
INSERT INTO permissions(operation, role_id)
SELECT '/api/appointment/cancel', r.id FROM roles r WHERE r.name IN ('ROLE_DOCTOR', 'ROLE_CHIEF');

-- different role functionality
INSERT INTO permissions(operation, role_id)
SELECT '/api/appointment/find', r.id FROM roles r WHERE r.name IN ('ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_CHIEF', 'ROLE_ADMIN');

------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------

INSERT INTO users(login, password, secret)
VALUES ('admin',
        '81ed7b2361b433013992acc48ecd9263:c8cc007f02f613d1b4cc89fe646a42aaceecb0b6ae1e7c184188663cebc66943',
        'ebb41dcbed87b0a79926487bc3134998:4c5849601d03fbb2ef84fbffb71257927a8cdb337b4cf48e897128c3b078276b'
       )
       ,('doctor',
        '81ed7b2361b433013992acc48ecd9263:c8cc007f02f613d1b4cc89fe646a42aaceecb0b6ae1e7c184188663cebc66943',
        'ebb41dcbed87b0a79926487bc3134998:4c5849601d03fbb2ef84fbffb71257927a8cdb337b4cf48e897128c3b078276b'
       )
       ,('patient',
        '81ed7b2361b433013992acc48ecd9263:c8cc007f02f613d1b4cc89fe646a42aaceecb0b6ae1e7c184188663cebc66943',
        'ebb41dcbed87b0a79926487bc3134998:4c5849601d03fbb2ef84fbffb71257927a8cdb337b4cf48e897128c3b078276b'
       )
       ;

------------------------------------------------------------------------------------------------------------------------

INSERT INTO user_roles(user_id, role_id, active)
SELECT u.id, r.id, TRUE FROM users u, roles r WHERE u.login = 'admin' AND r.isAdmin LIMIT 1;

--INSERT INTO user_roles(user_id, role_id, active)
--SELECT u.id, r.id, TRUE FROM users u, roles r WHERE u.login = 'doctor' AND r.name = 'ROLE_PATIENT' LIMIT 1;

INSERT INTO user_roles(user_id, role_id, active)
SELECT u.id, r.id, TRUE FROM users u, roles r WHERE u.login = 'doctor' AND r.name = 'ROLE_DOCTOR' LIMIT 1;

INSERT INTO user_roles(user_id, role_id, active)
SELECT u.id, r.id, TRUE FROM users u, roles r WHERE u.login = 'patient' AND r.name = 'ROLE_PATIENT' LIMIT 1;

------------------------------------------------------------------------------------------------------------------------

INSERT INTO user_info(user_id, firstName, secondName, description)
SELECT u.id, 'Антон', 'Шпак', 'Стоматолог' FROM users u WHERE u.login = 'doctor'  LIMIT 1;

INSERT INTO user_info(user_id, firstName, secondName, description)
SELECT u.id, 'Иван', 'Бунша', 'Управдом' FROM users u WHERE u.login = 'patient'  LIMIT 1;

------------------------------------------------------------------------------------------------------------------------

INSERT INTO appointment(dateTime, doctor_id,  doctorFirstName, doctorSecondName, doctordescription)
SELECT TO_TIMESTAMP('2021-06-01 11:00:00', 'YYYY-MM-DD HH:MI:SS'),
       u_d.id, u_i_d.firstname, u_i_d.secondname, u_i_d.description
FROM users u_d
         LEFT JOIN user_info u_i_d on u_d.id = u_i_d.user_id
WHERE u_d.login = 'doctor'
LIMIT 1;


INSERT INTO appointment(dateTime, accessCode, doctor_id, patient_id, doctorFirstName, doctorSecondName, doctordescription,
                        patientFirstName, patientSecondName, patientDescription)
SELECT TO_TIMESTAMP('2021-06-01 10:00:00', 'YYYY-MM-DD HH:MI:SS'), '88888888',
       u_d.id, u_p.id, u_i_d.firstname, u_i_d.secondname, u_i_d.description, u_i_p.firstname, u_i_p.secondname, u_i_p.description
FROM users u_d
JOIN users u_p ON u_d.login = 'doctor' AND u_p.login = 'patient'
    LEFT JOIN user_info u_i_d on u_d.id = u_i_d.user_id
    LEFT JOIN user_info u_i_p ON u_p.id = u_i_p.user_id
LIMIT 1;



