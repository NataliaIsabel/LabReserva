package br.edu.ifsp.labreserva.dao;

import br.edu.ifsp.labreserva.model.Reserva;
import br.edu.ifsp.labreserva.model.enums.StatusReserva;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservaDAO {

    /** SELECT base com JOIN para trazer código/nome do laboratório e nome do usuário. */
    private static final String SELECT_BASE =
            "SELECT r.id, r.laboratorio_id, r.usuario_id, r.data, r.hora_inicio, r.hora_fim, r.participantes, "
          + "       r.finalidade, r.status, r.criada_em, r.grupo_recorrencia, "
          + "       l.codigo AS lab_codigo, l.nome AS lab_nome, u.nome AS usuario_nome "
          + "FROM reservas r "
          + "JOIN laboratorios l ON l.id = r.laboratorio_id "
          + "JOIN usuarios u ON u.id = r.usuario_id ";

    private static final String ATIVAS = "r.status IN ('PENDENTE', 'CONFIRMADA')";

    // ------------------------------------------------------------------ escrita

    public Reserva inserir(Connection c, Reserva r) throws SQLException {
        String sql = "INSERT INTO reservas (laboratorio_id, usuario_id, data, hora_inicio, hora_fim, participantes, "
                   + "finalidade, status, criada_em, grupo_recorrencia) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        if (r.getCriadaEm() == null) r.setCriadaEm(LocalDateTime.now().withNano(0));
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.getLaboratorioId());
            ps.setLong(2, r.getUsuarioId());
            ps.setDate(3, Date.valueOf(r.getData()));
            ps.setTime(4, Time.valueOf(r.getHoraInicio()));
            ps.setTime(5, Time.valueOf(r.getHoraFim()));
            ps.setInt(6, r.getParticipantes());
            ps.setString(7, r.getFinalidade());
            ps.setString(8, r.getStatus().name());
            ps.setTimestamp(9, Timestamp.valueOf(r.getCriadaEm()));
            ps.setString(10, r.getGrupoRecorrencia());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {   // J04
                if (rs.next()) r.setId(rs.getLong(1));
            }
        }
        return r;
    }

    public boolean atualizarStatus(Connection c, long id, StatusReserva novo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE reservas SET status = ? WHERE id = ?")) {
            ps.setString(1, novo.name());
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    // ------------------------------------------------------------------ consultas (J03)

    public Optional<Reserva> buscarPorId(Connection c, long id) throws SQLException {
        List<Reserva> l = listar(c, SELECT_BASE + "WHERE r.id = ?", ps -> ps.setLong(1, id));
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    /** J03: agenda (reservas ativas) de um laboratório num intervalo de datas. */
    public List<Reserva> agenda(Connection c, long laboratorioId, LocalDate de, LocalDate ate) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.laboratorio_id = ? AND r.data BETWEEN ? AND ? AND " + ATIVAS
                   + " ORDER BY r.data, r.hora_inicio";
        return listar(c, sql, ps -> {
            ps.setLong(1, laboratorioId);
            ps.setDate(2, Date.valueOf(de));
            ps.setDate(3, Date.valueOf(ate));
        });
    }

    /** J03: reservas de um usuário (todas, mais recentes primeiro). */
    public List<Reserva> doUsuario(Connection c, long usuarioId) throws SQLException {
        return listar(c, SELECT_BASE + "WHERE r.usuario_id = ? ORDER BY r.data DESC, r.hora_inicio DESC",
                ps -> ps.setLong(1, usuarioId));
    }

    /** J03: reservas pendentes (as que ainda não passaram primeiro). */
    public List<Reserva> pendentes(Connection c) throws SQLException {
        return listar(c, SELECT_BASE + "WHERE r.status = 'PENDENTE' ORDER BY r.data, r.hora_inicio", ps -> { });
    }

    /**
     * J03: reservas ativas do laboratório que se sobrepõem à faixa [inicio, fim).
     * Sobrepõe quando começa antes do fim da outra E termina depois do início dela;
     * por isso 08–10 e 10–12 NÃO conflitam.
     * @param ignorarId reserva a desconsiderar (a própria, ao aprovar) ou null
     */
    public List<Reserva> sobrepostas(Connection c, long laboratorioId, LocalDate data, LocalTime inicio,
                                     LocalTime fim, Long ignorarId) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.laboratorio_id = ? AND r.data = ? AND " + ATIVAS
                   + " AND r.hora_inicio < ? AND r.hora_fim > ?"
                   + (ignorarId != null ? " AND r.id <> ?" : "");
        return listar(c, sql, ps -> {
            ps.setLong(1, laboratorioId);
            ps.setDate(2, Date.valueOf(data));
            ps.setTime(3, Time.valueOf(fim));
            ps.setTime(4, Time.valueOf(inicio));
            if (ignorarId != null) ps.setLong(5, ignorarId);
        });
    }

    /** Quantas reservas ativas o usuário tem com início a partir de "agora" (limite do aluno). */
    public int contarFuturasAtivas(Connection c, long usuarioId, LocalDateTime agora) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas r WHERE r.usuario_id = ? AND " + ATIVAS
                   + " AND (r.data > ? OR (r.data = ? AND r.hora_inicio > ?))";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ps.setDate(2, Date.valueOf(agora.toLocalDate()));
            ps.setDate(3, Date.valueOf(agora.toLocalDate()));
            ps.setTime(4, Time.valueOf(agora.toLocalTime().withNano(0)));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Listagem do admin na API, com filtros opcionais de status e laboratório. */
    public List<Reserva> listarComFiltros(Connection c, StatusReserva status, Long laboratorioId) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1");
        if (status != null) sql.append(" AND r.status = ?");
        if (laboratorioId != null) sql.append(" AND r.laboratorio_id = ?");
        sql.append(" ORDER BY r.data DESC, r.hora_inicio DESC");
        return listar(c, sql.toString(), ps -> {
            int i = 1;
            if (status != null) ps.setString(i++, status.name());
            if (laboratorioId != null) ps.setLong(i, laboratorioId);
        });
    }

    // ------------------------------------------------------------------ auxiliares

    private interface Parametros { void aplicar(PreparedStatement ps) throws SQLException; }

    private List<Reserva> listar(Connection c, String sql, Parametros p) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            p.aplicar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setId(rs.getLong("id"));
        r.setLaboratorioId(rs.getLong("laboratorio_id"));
        r.setUsuarioId(rs.getLong("usuario_id"));
        r.setData(rs.getDate("data").toLocalDate());
        r.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        r.setHoraFim(rs.getTime("hora_fim").toLocalTime());
        r.setParticipantes(rs.getInt("participantes"));
        r.setFinalidade(rs.getString("finalidade"));
        r.setStatus(StatusReserva.valueOf(rs.getString("status")));
        Timestamp criada = rs.getTimestamp("criada_em");
        r.setCriadaEm(criada != null ? criada.toLocalDateTime() : null);
        r.setGrupoRecorrencia(rs.getString("grupo_recorrencia"));
        r.setLaboratorioCodigo(rs.getString("lab_codigo"));
        r.setLaboratorioNome(rs.getString("lab_nome"));
        r.setUsuarioNome(rs.getString("usuario_nome"));
        return r;
    }
}
