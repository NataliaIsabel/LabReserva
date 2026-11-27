package br.edu.ifsp.labreserva.dao;

/** Falha de infraestrutura (SQL). Vira 500 na web e na API. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
