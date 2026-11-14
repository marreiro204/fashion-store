package br.com.fashionstore.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carrinho de compras do visitante/cliente.
 *
 * Não é uma tabela do banco: o carrinho fica guardado na SESSÃO
 * (HttpSession) até o pedido ser finalizado. Assim o visitante pode
 * montar o carrinho antes de fazer login, e o carrinho continua lá
 * depois do login.
 *
 * Cada item é um ItemPedido (produto + tamanho + quantidade), como
 * planejado na Etapa 2. O mesmo produto em tamanhos diferentes são itens
 * diferentes: "Camiseta P" e "Camiseta M".
 */
public class Carrinho implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Limite de unidades por item (produto + tamanho) em uma compra. */
    public static final int MAXIMO_POR_ITEM = 10;

    private final List<ItemPedido> itens = new ArrayList<>();

    /** Lista somente leitura: alterações passam pelos métodos abaixo. */
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /** @return o item do produto naquele tamanho, ou null */
    public ItemPedido buscarItem(int idProduto, String tamanho) {
        for (ItemPedido item : itens) {
            if (item.getProduto().getIdProduto() == idProduto && item.getTamanho().equalsIgnoreCase(tamanho)) {
                return item;
            }
        }
        return null;
    }

    /** Adiciona; se o produto+tamanho já estiver no carrinho, soma a quantidade. */
    public void adicionar(Produto produto, String tamanho, int quantidade) {
        ItemPedido existente = buscarItem(produto.getIdProduto(), tamanho);
        if (existente != null) {
            existente.setQuantidade(existente.getQuantidade() + quantidade);
        } else {
            itens.add(new ItemPedido(produto, tamanho, quantidade));
        }
    }

    /** Define a quantidade; zero ou menos remove o item. */
    public void alterarQuantidade(int idProduto, String tamanho, int quantidade) {
        if (quantidade <= 0) {
            remover(idProduto, tamanho);
            return;
        }
        ItemPedido item = buscarItem(idProduto, tamanho);
        if (item != null) {
            item.setQuantidade(quantidade);
        }
    }

    /** @return true se o item existia */
    public boolean remover(int idProduto, String tamanho) {
        return itens.removeIf(i -> i.getProduto().getIdProduto() == idProduto
                && i.getTamanho().equalsIgnoreCase(tamanho));
    }

    public void limpar() {
        itens.clear();
    }

    /** Na JSP: ${carrinho.vazio} */
    public boolean isVazio() {
        return itens.isEmpty();
    }

    /** Total de peças (ex.: 2 camisetas + 1 calça = 3). Usado no contador do menu. */
    public int getQuantidadeItens() {
        int total = 0;
        for (ItemPedido item : itens) {
            total += item.getQuantidade();
        }
        return total;
    }

    /** Soma dos subtotais. */
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }
}
