package br.edu.ifsp.labreserva.model;

import br.edu.ifsp.labreserva.model.enums.StatusReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reserva {
    private Long id;
    private Long laboratorioId;
    private Long usuarioId;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private int participantes;
    private String finalidade;
    private StatusReserva status;
    private LocalDateTime criadaEm;
    private String grupoRecorrencia;

    // Preenchidos por JOIN nas consultas de leitura (não são colunas de "reservas")
    private String laboratorioCodigo;
    private String laboratorioNome;
    private String usuarioNome;

    public Reserva() { }

    /** Cópia usada para gerar as ocorrências de uma reserva recorrente. */
    public Reserva copiar() {
        Reserva r = new Reserva();
        r.laboratorioId = laboratorioId;
        r.usuarioId = usuarioId;
        r.data = data;
        r.horaInicio = horaInicio;
        r.horaFim = horaFim;
        r.participantes = participantes;
        r.finalidade = finalidade;
        r.status = status;
        r.grupoRecorrencia = grupoRecorrencia;
        return r;
    }

    public LocalDateTime getInicio() {
        return (data == null || horaInicio == null) ? null : LocalDateTime.of(data, horaInicio);
    }

    public int getDuracaoHoras() {
        return horaFim.getHour() - horaInicio.getHour();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLaboratorioId() { return laboratorioId; }
    public void setLaboratorioId(Long laboratorioId) { this.laboratorioId = laboratorioId; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public int getParticipantes() { return participantes; }
    public void setParticipantes(int participantes) { this.participantes = participantes; }
    public String getFinalidade() { return finalidade; }
    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }
    public StatusReserva getStatus() { return status; }
    public void setStatus(StatusReserva status) { this.status = status; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public String getGrupoRecorrencia() { return grupoRecorrencia; }
    public void setGrupoRecorrencia(String grupoRecorrencia) { this.grupoRecorrencia = grupoRecorrencia; }
    public String getLaboratorioCodigo() { return laboratorioCodigo; }
    public void setLaboratorioCodigo(String laboratorioCodigo) { this.laboratorioCodigo = laboratorioCodigo; }
    public String getLaboratorioNome() { return laboratorioNome; }
    public void setLaboratorioNome(String laboratorioNome) { this.laboratorioNome = laboratorioNome; }
    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
}
