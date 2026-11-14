package br.com.fashionstore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.model.StatusPedido;

/**
 * Números do painel administrativo (montados pelo PainelService).
 *
 * É uma classe comum com getters (e não um "record" do Java), porque a
 * EL das JSPs do Tomcat 10.1 lê propriedades pelos métodos getXxx().
 */
public class ResumoPainel {

    private int totalClientes;
    private int totalProdutos;
    private int totalPedidos;
    private BigDecimal faturamento = BigDecimal.ZERO;
    private final Map<StatusPedido, Integer> pedidosPorStatus = new EnumMap<>(StatusPedido.class);
    private final List<Produto> produtosEsgotados = new ArrayList<>();
    private final List<Produto> produtosEstoqueBaixo = new ArrayList<>();
    private final List<Pedido> ultimosPedidos = new ArrayList<>();

    public ResumoPainel() {
        for (StatusPedido s : StatusPedido.values()) {
            pedidosPorStatus.put(s, 0);
        }
    }

    public int getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(int totalClientes) {
        this.totalClientes = totalClientes;
    }

    public int getTotalProdutos() {
        return totalProdutos;
    }

    public void setTotalProdutos(int totalProdutos) {
        this.totalProdutos = totalProdutos;
    }

    public int getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(int totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    /** Soma dos pedidos não cancelados. */
    public BigDecimal getFaturamento() {
        return faturamento;
    }

    public void setFaturamento(BigDecimal faturamento) {
        this.faturamento = faturamento;
    }

    /**
     * Quantidade de pedidos em cada status, na ordem do enum.
     * Na JSP: <c:forEach var="item" items="${resumo.pedidosPorStatus}"> ${item.key.descricao}: ${item.value}
     */
    public Map<StatusPedido, Integer> getPedidosPorStatus() {
        return pedidosPorStatus;
    }

    /** Pedidos aguardando ação do admin (pendentes + pagos). */
    public int getPedidosEmAberto() {
        return pedidosPorStatus.get(StatusPedido.PENDENTE) + pedidosPorStatus.get(StatusPedido.PAGO);
    }

    public List<Produto> getProdutosEsgotados() {
        return produtosEsgotados;
    }

    public List<Produto> getProdutosEstoqueBaixo() {
        return produtosEstoqueBaixo;
    }

    public List<Pedido> getUltimosPedidos() {
        return ultimosPedidos;
    }
}
