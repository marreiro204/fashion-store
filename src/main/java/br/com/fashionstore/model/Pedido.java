package br.com.fashionstore.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido feito por um cliente.
 * Tabela: pedido (id_pedido, id_cliente, id_endereco, data_pedido,
 *                 valor_total, status).
 *
 * Ajustes menores:
 * - dataPedido usa LocalDateTime (data + hora). No banco: DATETIME.
 * - status usa o enum StatusPedido em vez de texto livre.
 *
 * Relacionamentos:
 * - N:1 com Cliente  (quem comprou)
 * - N:1 com Endereco (para onde entregar)
 * - 1:N com ItemPedido (o que foi comprado)
 */
public class Pedido implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idPedido;
    private Cliente cliente;
    private Endereco endereco;
    private LocalDateTime dataPedido = LocalDateTime.now();
    private BigDecimal valorTotal = BigDecimal.ZERO;
    private StatusPedido status = StatusPedido.PENDENTE; // todo pedido nasce pendente
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(Cliente cliente, Endereco endereco) {
        this.cliente = cliente;
        this.endereco = endereco;
    }

    // ===== Métodos de negócio =====

    /** Adiciona um item e atualiza o valor total. */
    public void adicionarItem(ItemPedido item) {
        item.setIdPedido(this.idPedido);
        itens.add(item);
        this.valorTotal = calcularValorTotal();
    }

    /** Soma dos subtotais de todos os itens. */
    public BigDecimal calcularValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    /**
     * Data no formato brasileiro: "14/11/2026 15:30".
     * (A tag fmt:formatDate do JSTL não aceita LocalDateTime, por isso o método.)
     */
    public String getDataFormatada() {
        return dataPedido == null ? "" : dataPedido.format(FORMATO_DATA);
    }

    private static final java.time.format.DateTimeFormatter FORMATO_DATA =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Quantidade total de peças do pedido. */
    public int getQuantidadeItens() {
        int total = 0;
        for (ItemPedido item : itens) {
            total += item.getQuantidade();
        }
        return total;
    }

    /**
     * Muda o status, impedindo alterar pedidos já entregues ou cancelados.
     * Usado pelo administrador na área de pedidos.
     */
    public void alterarStatus(StatusPedido novoStatus) {
        if (status.isFinal()) {
            throw new IllegalStateException("O pedido já está " + status.getDescricao()
                    + " e não pode mais ser alterado.");
        }
        if (!status.podeMudarPara(novoStatus)) {
            throw new IllegalStateException("Não é possível mudar de " + status.getDescricao()
                    + " para " + novoStatus.getDescricao() + ".");
        }
        this.status = novoStatus;
    }

    // ===== Getters e setters =====

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    /**
     * Se o pedido tem itens carregados, o total é sempre recalculado a partir
     * deles (assim, mudar a quantidade de um item já atualiza o total).
     * Se não tem itens carregados (ex.: listagem de pedidos vinda do banco),
     * retorna o valor_total gravado.
     */
    public BigDecimal getValorTotal() {
        return itens.isEmpty() ? valorTotal : calcularValorTotal();
    }

    /** Usado pelo DAO ao ler o valor gravado no banco. */
    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusPedido getStatus() {
        return status;
    }

    /** Usado pelo DAO ao ler do banco. Para mudanças do admin use alterarStatus(). */
    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
        this.valorTotal = calcularValorTotal();
    }

    @Override
    public String toString() {
        return "Pedido{id=" + idPedido
                + ", cliente=" + (cliente != null ? cliente.getNome() : "-")
                + ", itens=" + itens.size() + ", valorTotal=" + getValorTotal()
                + ", status=" + status + "}";
    }
}
