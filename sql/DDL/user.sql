CREATE USER 'migraflow'@'%' IDENTIFIED BY 'migraflow';

GRANT ALL PRIVILEGES
    ON migraflow.*
    TO 'migraflow'@'%';

FLUSH PRIVILEGES;

CREATE DATABASE migraflow
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
