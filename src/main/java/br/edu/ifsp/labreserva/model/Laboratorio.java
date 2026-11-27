package br.edu.ifsp.labreserva.model;

public class Laboratorio {
    private Long id;
    private String codigo;
    private String nome;
    private int capacidade;
    private String recursos;
    private boolean ativo = true;

    public Laboratorio() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getCapacidade() { return capacidade; }
    public void setCapacidade(int capacidade) { this.capacidade = capacidade; }
    public String getRecursos() { return recursos; }
    public void setRecursos(String recursos) { this.recursos = recursos; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
