package br.edu.ifsp.labreserva.model.enums;

/** Perfis de acesso (seção 2 do enunciado). */
public enum Perfil {
    ALUNO, PROFESSOR, ADMIN;

    /** Duração máxima de uma reserva, em horas. ADMIN segue apenas o horário de funcionamento. */
    public int duracaoMaximaHoras() {
        return switch (this) {
            case ALUNO -> 2;
            case PROFESSOR -> 4;
            case ADMIN -> 16;
        };
    }

    public boolean podeReservaRecorrente() {
        return this == PROFESSOR || this == ADMIN;
    }
}
