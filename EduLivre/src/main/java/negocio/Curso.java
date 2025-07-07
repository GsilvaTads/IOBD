package negocio;

import java.time.LocalDate;
import java.util.UUID;

import org.json.JSONObject;

public class Curso {
    private UUID id;
    private String titulo;
    private String descricao;
    private LocalDate dataCriacao;
    private JSONObject avaliacao;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public JSONObject getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(JSONObject avaliacao) {
        this.avaliacao = avaliacao;
    }
}