package br.com.fashionstore.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Peça de roupa vendida pela loja.
 * Tabela: produto (id_produto, id_categoria, nome, descricao, preco, imagem).
 *
 * Ajuste aprovado (Proposta 1): o campo "tamanho" NÃO fica mais aqui.
 * Os tamanhos e suas quantidades estão na lista "estoques".
 *
 * Por que BigDecimal no preço e não double?
 * double tem erros de arredondamento (0.1 + 0.2 = 0.30000000000000004).
 * Com dinheiro isso é inaceitável; BigDecimal faz contas decimais exatas
 * e corresponde ao tipo DECIMAL do MySQL.
 */
public class Produto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idProduto;
    private Categoria categoria;     // N:1 -> muitos produtos pertencem a uma categoria
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private String imagem;           // nome do arquivo ou URL da imagem

    // 1:N -> um produto tem uma linha de estoque por tamanho
    private List<Estoque> estoques = new ArrayList<>();

    public Produto() {
    }

    public Produto(Categoria categoria, String nome, String descricao,
                   BigDecimal preco, String imagem) {
        this.categoria = categoria;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.imagem = imagem;
    }

    // ===== Métodos de negócio =====

    /** Adiciona um tamanho ao produto, já ligado a este produto. */
    public void adicionarEstoque(Estoque estoque) {
        estoque.setIdProduto(this.idProduto);
        estoques.add(estoque);
    }

    /** Procura o estoque de um tamanho. Retorna null se o tamanho não existir. */
    public Estoque getEstoquePorTamanho(String tamanho) {
        for (Estoque e : estoques) {
            if (e.getTamanho().equalsIgnoreCase(tamanho)) {
                return e;
            }
        }
        return null;
    }

    /** Quantidade disponível de um tamanho (0 se o tamanho não existir). */
    public int getQuantidadeDisponivel(String tamanho) {
        Estoque e = getEstoquePorTamanho(tamanho);
        return e == null ? 0 : e.getQuantidade();
    }

    /** Soma de todos os tamanhos. */
    public int getQuantidadeTotal() {
        int total = 0;
        for (Estoque e : estoques) {
            total += e.getQuantidade();
        }
        return total;
    }

    /** Tamanhos que ainda têm pelo menos 1 unidade (para montar o seletor na página). */
    public List<String> getTamanhosDisponiveis() {
        List<String> tamanhos = new ArrayList<>();
        for (Estoque e : estoques) {
            if (e.getQuantidade() > 0) {
                tamanhos.add(e.getTamanho());
            }
        }
        return tamanhos;
    }

    /** Produto esgotado em todos os tamanhos? (EL: ${produto.esgotado}) */
    public boolean isEsgotado() {
        return getQuantidadeTotal() == 0;
    }

    // ===== Getters e setters =====

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public List<Estoque> getEstoques() {
        return estoques;
    }

    public void setEstoques(List<Estoque> estoques) {
        this.estoques = estoques;
    }

    @Override
    public String toString() {
        return "Produto{id=" + idProduto + ", nome='" + nome + "', preco=" + preco
                + ", categoria=" + (categoria != null ? categoria.getNome() : "-") + "}";
    }
}
