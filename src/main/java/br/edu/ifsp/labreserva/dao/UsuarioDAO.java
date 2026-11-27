package br.edu.ifsp.labreserva.dao;

import br.edu.ifsp.labreserva.model.Usuario;
import br.edu.ifsp.labreserva.model.enums.Perfil;

import java.sql.*;
import java.util.Optional;

/**
 * J02: todo SQL com PreparedStatement. A Connection vem do serviço, que
 * decide o início e o fim da transação; o DAO nunca abre nem fecha conexão.
 */
public class UsuarioDAO {

    private static final String COLUNAS = "id, nome, email, senha_hash, perfil";

    public Usuario inserir(Connection c, Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, email, senha_hash, perfil) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSenhaHash());
            ps.setString(4, u.getPerfil().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {   // J04
                if (rs.next()) u.setId(rs.getLong(1));
            }
        }
        return u;
    }

    public Optional<Usuario> buscarPorEmail(Connection c, String email) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM usuarios WHERE email = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Usuario> buscarPorId(Connection c, long id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getLong("id"), rs.getString("nome"), rs.getString("email"),
                rs.getString("senha_hash"), Perfil.valueOf(rs.getString("perfil")));
    }
}
