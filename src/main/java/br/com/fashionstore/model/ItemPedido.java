package br.com.fashionstore.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Um produto, em um tamanho, dentro de um pedido.
 * Tabela: item_pedido (id_item_pedido, id_pedido, id_produto, tamanho,
 *                      quantidade, preco_unitario, subtotal).
 *
 * Ajuste aprovado (Proposta 1): o item guarda o TAMANHO escolhido.
 *
 * Regras da documentação:
 * - precoUnitario = preço do produto no momento da compra (se o preço do
 *   produto mudar depois, o pedido antigo continua com o valor pago);
 * - subtotal = quantidade x precoUnitario.
 *
 * Esta mesma classe também servirá como item do carrinho na Etapa 7.
 */
public class ItemPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idItemPedido;
    private int idPedido;
    private Produto produto;          // N:1 -> o produto deste item
    private String tamanho;
    private int quantidade;
    private BigDecimal precoUnitario = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;

    public ItemPedido() {
    }

    /** Cria o item copiando o preço atual do produto e já calculando o subtotal. */
    public ItemPedido(Produto produto, String tamanho, int quantidade) {
        this.produto = produto;
        this.tamanho = tamanho;
        this.precoUnitario = produto.getPreco();
        setQuantidade(quantidade);
    }

    // ===== Métodos de negócio =====

    /** subtotal = quantidade x precoUnitario */
    public BigDecimal calcularSubtotal() {
        if (precoUnitario == null) {
            return BigDecimal.ZERO;
        }
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    // ===== Getters e setters =====

    public int getIdItemPedido() {
        return idItemPedido;
    }

    public void setIdItemPedido(int idItemPedido) {
        this.idItemPedido = idItemPedido;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
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

    /** Ao mudar a quantidade, o subtotal é recalculado automaticamente. */
    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        this.quantidade = quantidade;
        this.subtotal = calcularSubtotal();
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
        this.subtotal = calcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    /** Usado pelo DAO ao ler o valor gravado no banco. */
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "ItemPedido{produto=" + (produto != null ? produto.getNome() : "-")
                + ", tamanho='" + tamanho + "', quantidade=" + quantidade
                + ", precoUnitario=" + precoUnitario + ", subtotal=" + subtotal + "}";
    }
}
