-- =====================================================================
-- LabReserva - schema.sql 
-- =====================================================================
DROP DATABASE IF EXISTS labreserva;
CREATE DATABASE labreserva CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE labreserva;

-- Usuário de aplicação (senha de TESTE; troque em produção)
CREATE USER IF NOT EXISTS 'labreserva'@'localhost' IDENTIFIED BY 'labreserva123';
GRANT SELECT, INSERT, UPDATE, DELETE ON labreserva.* TO 'labreserva'@'localhost';
FLUSH PRIVILEGES;

CREATE TABLE usuarios (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    senha_hash  VARCHAR(255) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_perfil CHECK (perfil IN ('ALUNO','PROFESSOR','ADMIN'))
) ENGINE=InnoDB;

CREATE TABLE laboratorios (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(10)  NOT NULL,
    nome        VARCHAR(100) NOT NULL,
    capacidade  INT          NOT NULL,
    recursos    VARCHAR(255),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_laboratorios PRIMARY KEY (id),
    CONSTRAINT uq_laboratorios_codigo UNIQUE (codigo),
    CONSTRAINT ck_laboratorios_capacidade CHECK (capacidade > 0)
) ENGINE=InnoDB;

CREATE TABLE reservas (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    laboratorio_id     BIGINT       NOT NULL,
    usuario_id         BIGINT       NOT NULL,
    data               DATE         NOT NULL,
    hora_inicio        TIME         NOT NULL,
    hora_fim           TIME         NOT NULL,
    participantes      INT          NOT NULL,
    finalidade         VARCHAR(200) NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    criada_em          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    grupo_recorrencia  VARCHAR(36)  NULL,
    CONSTRAINT pk_reservas PRIMARY KEY (id),
    CONSTRAINT fk_reservas_laboratorio FOREIGN KEY (laboratorio_id) REFERENCES laboratorios (id),
    CONSTRAINT fk_reservas_usuario     FOREIGN KEY (usuario_id)     REFERENCES usuarios (id),
    CONSTRAINT ck_reservas_status CHECK (status IN ('PENDENTE','CONFIRMADA','RECUSADA','CANCELADA')),
    CONSTRAINT ck_reservas_horario CHECK (hora_fim > hora_inicio),
    CONSTRAINT ck_reservas_participantes CHECK (participantes > 0)
) ENGINE=InnoDB;

CREATE INDEX ix_reservas_lab_data ON reservas (laboratorio_id, data);
CREATE INDEX ix_reservas_usuario  ON reservas (usuario_id);
CREATE INDEX ix_reservas_status   ON reservas (status);

-- ---------------------------------------------------------------------
-- Dados de teste
-- Senhas (somente teste): admin123 / prof123 / aluno123
-- Hash: PBKDF2-HMAC-SHA256, 120000 iterações (ver SenhaHasher.java)
-- ---------------------------------------------------------------------
INSERT INTO usuarios (id, nome, email, senha_hash, perfil) VALUES
 (1, 'Administrador LabReserva', 'admin@labreserva.edu.br',  'pbkdf2_sha256$120000$bGFicmVzZXJ2YS1hZG0wMQ==$y1Z/y3GTZFtqdwj6cc1eJ2CZPdnYf/UhLHkLjiFvA50=', 'ADMIN'),
 (2, 'Carlos Pereira',           'carlos@labreserva.edu.br', 'pbkdf2_sha256$120000$bGFicmVzZXJ2YS1wcmYwMQ==$X2h3Xc4IwfP2Xbuw6XAIo9o5b5NhWhydz2LSF9K+Sxw=', 'PROFESSOR'),
 (3, 'Marina Souza',             'marina@labreserva.edu.br', 'pbkdf2_sha256$120000$bGFicmVzZXJ2YS1wcmYwMg==$tdlzs+96uhQmrpBulNDOWAqJ74OUaiSnNxA414G4T/M=', 'PROFESSOR'),
 (4, 'Ana Lima',                 'ana@labreserva.edu.br',    'pbkdf2_sha256$120000$bGFicmVzZXJ2YS1hbHUwMQ==$w8/1MycGeyMQrEwpspMlBaU/0E8YECZ5u2Plw6Y4fmI=', 'ALUNO'),
 (5, 'Bruno Costa',              'bruno@labreserva.edu.br',  'pbkdf2_sha256$120000$bGFicmVzZXJ2YS1hbHUwMg==$YjIOkMEFqxtWQ8KVdfH2UCuJajy8VCdbNVvHkMbPnWs=', 'ALUNO'),
 (6, 'Camila Rocha',             'camila@labreserva.edu.br', 'pbkdf2_sha256$120000$bGFicmVzZXJ2YS1hbHUwMw==$3C7UFAh8Tb1igC89hj+xqoDLni1fvGB1S2ny4uiCaC8=', 'ALUNO');

INSERT INTO laboratorios (id, codigo, nome, capacidade, recursos, ativo) VALUES
 (1, 'LAB01', 'Laboratório de Informática 1', 30, '30 PCs, projetor, lousa digital', TRUE),
 (2, 'LAB02', 'Laboratório de Redes',         20, 'Switches, roteadores, racks',     TRUE),
 (3, 'LAB03', 'Laboratório de Hardware',      15, 'Bancadas, multímetros, osciloscópios', TRUE),
 (4, 'LAB04', 'Laboratório Antigo (reforma)', 10, 'Em reforma',                      FALSE);

-- @seg = próxima segunda-feira (1 a 7 dias à frente de hoje)
SET @seg = DATE_ADD(CURDATE(), INTERVAL (7 - WEEKDAY(CURDATE())) DAY);

INSERT INTO reservas (id, laboratorio_id, usuario_id, data, hora_inicio, hora_fim, participantes, finalidade, status, grupo_recorrencia) VALUES
 -- futuras
 (1, 1, 2, @seg,                          '08:00', '10:00', 25, 'Aula de Programação Orientada a Objetos', 'CONFIRMADA', NULL),
 (2, 2, 5, @seg,                          '14:00', '16:00',  4, 'Estudo para a prova de Redes',            'PENDENTE',   NULL),
 -- (3) pendente que "passou a conflitar" com a (1): usada no teste de 409 ao aprovar
 (3, 1, 5, @seg,                          '09:00', '11:00',  3, 'Trabalho em grupo de Banco de Dados',     'PENDENTE',   NULL),
 (4, 3, 6, DATE_ADD(@seg, INTERVAL 1 DAY), '10:00', '12:00',  2, 'Montagem do projeto de Arduino',          'RECUSADA',   NULL),
 (5, 2, 3, DATE_ADD(@seg, INTERVAL 2 DAY), '19:00', '21:00', 18, 'Aula prática de VLANs',                   'CANCELADA',  NULL),
 -- recorrência (2 semanas, mesma faixa)
 (6, 3, 3, DATE_ADD(@seg, INTERVAL 3 DAY),  '08:00', '10:00', 12, 'Aula de Eletrônica Digital', 'CONFIRMADA', 'seed-recorrencia-0001'),
 (7, 3, 3, DATE_ADD(@seg, INTERVAL 10 DAY), '08:00', '10:00', 12, 'Aula de Eletrônica Digital', 'CONFIRMADA', 'seed-recorrencia-0001'),
 -- passadas
 (8,  1, 2, DATE_SUB(@seg, INTERVAL 14 DAY), '08:00', '10:00', 28, 'Aula de Algoritmos',              'CONFIRMADA', NULL),
 (9,  2, 4, DATE_SUB(@seg, INTERVAL 13 DAY), '10:00', '12:00',  3, 'Configuração de servidor DHCP',   'CONFIRMADA', NULL),
 (10, 3, 5, DATE_SUB(@seg, INTERVAL 12 DAY), '14:00', '16:00',  2, 'Teste de fontes de alimentação',  'RECUSADA',   NULL),
 (11, 1, 6, DATE_SUB(@seg, INTERVAL 11 DAY), '16:00', '18:00',  5, 'Ensaio de apresentação de TCC',   'CANCELADA',  NULL),
 (12, 2, 4, DATE_SUB(@seg, INTERVAL 10 DAY), '08:00', '09:00',  2, 'Revisão de cabeamento',           'PENDENTE',   NULL);

-- garante que os próximos ids gerados não colidam com os fixos acima
ALTER TABLE usuarios     AUTO_INCREMENT = 100;
ALTER TABLE laboratorios AUTO_INCREMENT = 100;
ALTER TABLE reservas     AUTO_INCREMENT = 100;
