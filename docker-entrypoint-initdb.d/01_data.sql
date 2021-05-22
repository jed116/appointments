INSERT INTO roles(name)
VALUES ('ROLE_CHEIF'),
       ('ROLE_DOCTOR'),
       ('ROLE_PATIENT'),
       ('ROLE_ADMIN');

INSERT INTO users(login, password, secret)
VALUES ('admin',
        '81ed7b2361b433013992acc48ecd9263:c8cc007f02f613d1b4cc89fe646a42aaceecb0b6ae1e7c184188663cebc66943',
        'ebb41dcbed87b0a79926487bc3134998:4c5849601d03fbb2ef84fbffb71257927a8cdb337b4cf48e897128c3b078276b'
        ),
       ('patient',
        '81ed7b2361b433013992acc48ecd9263:c8cc007f02f613d1b4cc89fe646a42aaceecb0b6ae1e7c184188663cebc66943',
        'ebb41dcbed87b0a79926487bc3134998:4c5849601d03fbb2ef84fbffb71257927a8cdb337b4cf48e897128c3b078276b'
       ),
       ('doctor',
        '81ed7b2361b433013992acc48ecd9263:c8cc007f02f613d1b4cc89fe646a42aaceecb0b6ae1e7c184188663cebc66943',
        'ebb41dcbed87b0a79926487bc3134998:4c5849601d03fbb2ef84fbffb71257927a8cdb337b4cf48e897128c3b078276b'
       );

INSERT INTO user_roles(user_id, role_id, active)
SELECT u.id, r.id, TRUE FROM users u, roles r WHERE u.login = 'admin' AND r.name = 'ROLE_ADMIN' LIMIT 1;

INSERT INTO user_roles(user_id, role_id, active)
SELECT u.id, r.id, TRUE FROM users u, roles r WHERE u.login = 'doctor' AND r.name = 'ROLE_PATIENT' LIMIT 1;

INSERT INTO user_roles(user_id, role_id, active)
SELECT u.id, r.id, FALSE FROM users u, roles r WHERE u.login = 'doctor' AND r.name = 'ROLE_DOCTOR' LIMIT 1;

INSERT INTO user_roles(user_id, role_id, active)
SELECT u.id, r.id, TRUE FROM users u, roles r WHERE u.login = 'patient' AND r.name = 'ROLE_PATIENT' LIMIT 1;


INSERT INTO user_info(user_id, firstName, secondName, description)
SELECT u.id, 'Антон', 'Шпак', 'Стоматолог' FROM users u WHERE u.login = 'doctor'  LIMIT 1;
