package br.edu.ifsp.labreserva.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexão por DriverManager, usada na ENTREGA PARCIAL.
 * Na entrega final (tema N) a web passa a usar JNDI com pool, e esta
 * classe é substituída por JndiConnectionFactory.
 */
public class ConexaoDireta implements ConnectionFactory {

    private final String url;
    private final String usuario;
    private final String senha;

    public ConexaoDireta(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
