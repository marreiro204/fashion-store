package br.com.fashionstore.model;

import java.io.Serializable;

/**
 * Categoria de produtos (ex.: Camisetas, Calças, Vestidos).
 * Tabela: categoria (id_categoria, nome, descricao).
 */
public class Categoria implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idCategoria;
    private String nome;
    private String descricao;

    // Construtor vazio: usado pelos DAOs e pelos Servlets ao montar o objeto aos poucos.
    public Categoria() {
    }

    // Construtor para criar uma categoria nova (ainda sem id, o banco gera).
    public Categoria(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    // Construtor completo: usado ao ler uma categoria do banco.
    public Categoria(int idCategoria, String nome, String descricao) {
        this.idCategoria = idCategoria;
        this.nome = nome;
        this.descricao = descricao;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Nome "limpo" para usar em nomes de arquivo e endereços:
     * "Calças Jeans" -> "calcas-jeans". É assim que a imagem da categoria
     * é encontrada: assets/images/categorias/calcas-jeans.jpg
     */
    public String getSlug() {
        if (nome == null) {
            return "";
        }
        String semAcento = java.text.Normalizer.normalize(nome, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    @Override
    public String toString() {
        return "Categoria{id=" + idCategoria + ", nome='" + nome + "'}";
    }
}
