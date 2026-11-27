package br.edu.ifsp.labreserva.model.enums;

public enum StatusReserva {
    PENDENTE, CONFIRMADA, RECUSADA, CANCELADA;

    /** Reservas ativas ocupam a faixa na agenda e entram na verificação de conflito. */
    public boolean isAtiva() {
        return this == PENDENTE || this == CONFIRMADA;
    }
}
