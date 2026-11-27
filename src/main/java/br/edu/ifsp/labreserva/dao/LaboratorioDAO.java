package br.edu.ifsp.labreserva.dao;

import br.edu.ifsp.labreserva.model.Laboratorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LaboratorioDAO {

    private static final String COLUNAS = "id, codigo, nome, capacidade, recursos, ativo";

    public Laboratorio inserir(Connection c, Laboratorio l) throws SQLException {
        String sql = "INSERT INTO laboratorios (codigo, nome, capacidade, recursos, ativo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, l);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {   // J04
                if (rs.next()) l.setId(rs.getLong(1));
            }
        }
        return l;
    }

    public boolean atualizar(Connection c, Laboratorio l) throws SQLException {
        String sql = "UPDATE laboratorios SET codigo = ?, nome = ?, capacidade = ?, recursos = ?, ativo = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            preencher(ps, l);
            ps.setLong(6, l.getId());
            return ps.executeUpdate() == 1;
        }
    }

    public Optional<Laboratorio> buscarPorId(Connection c, long id) throws SQLException {
        return buscarUm(c, "SELECT " + COLUNAS + " FROM laboratorios WHERE id = ?", ps -> ps.setLong(1, id));
    }

    public Optional<Laboratorio> buscarPorCodigo(Connection c, String codigo) throws SQLException {
        return buscarUm(c, "SELECT " + COLUNAS + " FROM laboratorios WHERE codigo = ?", ps -> ps.setString(1, codigo));
    }

    public List<Laboratorio> listar(Connection c, boolean somenteAtivos) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM laboratorios"
                + (somenteAtivos ? " WHERE ativo = TRUE" : "") + " ORDER BY codigo";
        List<Laboratorio> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private void preencher(PreparedStatement ps, Laboratorio l) throws SQLException {
        ps.setString(1, l.getCodigo());
        ps.setString(2, l.getNome());
        ps.setInt(3, l.getCapacidade());
        ps.setString(4, l.getRecursos());
        ps.setBoolean(5, l.isAtivo());
    }

    private interface Parametros { void aplicar(PreparedStatement ps) throws SQLException; }

    private Optional<Laboratorio> buscarUm(Connection c, String sql, Parametros p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            p.aplicar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    private Laboratorio mapear(ResultSet rs) throws SQLException {
        Laboratorio l = new Laboratorio();
        l.setId(rs.getLong("id"));
        l.setCodigo(rs.getString("codigo"));
        l.setNome(rs.getString("nome"));
        l.setCapacidade(rs.getInt("capacidade"));
        l.setRecursos(rs.getString("recursos"));
        l.setAtivo(rs.getBoolean("ativo"));
        return l;
    }
}
