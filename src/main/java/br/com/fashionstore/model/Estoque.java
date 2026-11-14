package br.com.fashionstore.model;

import java.io.Serializable;

/**
 * Quantidade disponível de um produto EM UM TAMANHO.
 * Tabela: estoque (id_estoque, id_produto, tamanho, quantidade).
 *
 * Ajuste aprovado (Proposta 1): o tamanho saiu de Produto e veio para cá.
 * Assim a "Camiseta Básica" tem uma linha de estoque para P, outra para M,
 * outra para G..., cada uma com sua quantidade.
 * No banco, a combinação (id_produto, tamanho) será única.
 */
public class Estoque implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idEstoque;
    private int idProduto;
    private String tamanho;   // P, M, G, GG, 38, 40...
    private int quantidade;

    public Estoque() {
    }

    public Estoque(int idProduto, String tamanho, int quantidade) {
        this.idProduto = idProduto;
        this.tamanho = tamanho;
        setQuantidade(quantidade);
    }

    /** Até quantas unidades o estoque é considerado "baixo" (alerta no admin). */
    public static final int LIMITE_ESTOQUE_BAIXO = 3;

    // ===== Métodos de negócio =====

    /** Tem poucas unidades (1 a 3)? Na JSP: ${e.estoqueBaixo} */
    public boolean isEstoqueBaixo() {
        return quantidade > 0 && quantidade <= LIMITE_ESTOQUE_BAIXO;
    }

    /** Existe a quantidade pedida neste tamanho? */
    public boolean temDisponivel(int quantidadeDesejada) {
        return quantidadeDesejada > 0 && quantidade >= quantidadeDesejada;
    }

    /** Retira itens do estoque (usado ao finalizar um pedido). */
    public void baixar(int quantidadeVendida) {
        if (quantidadeVendida <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (!temDisponivel(quantidadeVendida)) {
            throw new IllegalStateException("Estoque insuficiente para o tamanho " + tamanho
                    + ". Disponível: " + quantidade);
        }
        quantidade -= quantidadeVendida;
    }

    /** Devolve itens ao estoque (reposição ou pedido cancelado). */
    public void repor(int quantidadeAdicionada) {
        if (quantidadeAdicionada <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        quantidade += quantidadeAdicionada;
    }

    // ===== Getters e setters =====

    public int getIdEstoque() {
        return idEstoque;
    }

    public void setIdEstoque(int idEstoque) {
        this.idEstoque = idEstoque;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public int getQuantidade() {
        return quantidade;
    }

    /** Estoque nunca pode ser negativo. */
    public void setQuantidade(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa.");
        }
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return "Estoque{produto=" + idProduto + ", tamanho='" + tamanho
                + "', quantidade=" + quantidade + "}";
    }
}
