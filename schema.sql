-- =====================================================================
-- LabReserva - schema.sql (MySQL 8)
-- Executar como root:   mysql -u root -p < schema.sql
-- Recria o banco do zero: tabelas (J01) + dados de teste.
-- As datas dos dados de teste são RELATIVAS à data de execução, para
-- que a coleção do Postman funcione em qualquer dia.
-- =====================================================================
DROP DATABASE IF EXISTS labreserva;
CREATE DATABASE labreserva CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE labreserva;

-- Usuário de aplicação (senha de TESTE; troque em produção)
CREATE USER IF NOT EXISTS 'labreserva'@'localhost' IDENTIFIED BY 'labreserva123';
GRANT SELECT, INSERT, UPDATE, DELETE ON labreserva.* TO 'labreserva'@'localhost';
FLUSH PRIVILEGES;

