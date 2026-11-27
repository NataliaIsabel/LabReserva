package br.edu.ifsp.labreserva.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * De onde vêm as conexões. Os serviços só conhecem esta interface,
 * então trocar DriverManager por JNDI (tema N) não muda nenhum serviço.
 */
@FunctionalInterface
public interface ConnectionFactory {
    Connection getConnection() throws SQLException;
}
