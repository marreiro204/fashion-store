package br.com.fashionstore.service;

import java.math.BigDecimal;
import java.util.List;

import br.com.fashionstore.dao.ClienteDAO;
import br.com.fashionstore.dao.PedidoDAO;
import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.model.StatusPedido;

/**
 * Monta o resumo do painel administrativo: clientes, produtos, pedidos
 * por status, faturamento e alertas de estoque.
 *
 * Os números são calculados em Java a partir das listagens dos DAOs. Para
 * o tamanho de uma loja acadêmica isso é simples e suficiente; em uma loja
 * grande, o ideal seria usar consultas SQL com COUNT/SUM.
 */
public class PainelService {

    private static final int QUANTIDADE_ULTIMOS_PEDIDOS = 5;

    private final PedidoDAO pedidoDAO;
    private final ClienteDAO clienteDAO;
    private final ProdutoDAO produtoDAO;

    public PainelService() {
        this(new PedidoDAO(), new ClienteDAO(), new ProdutoDAO());
    }

    public PainelService(PedidoDAO pedidoDAO, ClienteDAO clienteDAO, ProdutoDAO produtoDAO) {
        this.pedidoDAO = pedidoDAO;
        this.clienteDAO = clienteDAO;
        this.produtoDAO = produtoDAO;
    }

    public ResumoPainel gerarResumo() {
        ResumoPainel resumo = new ResumoPainel();

        // Clientes (sem contar administradores)
        int clientes = 0;
        for (Cliente c : clienteDAO.listarTodos()) {
            if (!c.isAdmin()) {
                clientes++;
            }
        }
        resumo.setTotalClientes(clientes);

        // Pedidos: já vêm do mais recente para o mais antigo
        List<Pedido> pedidos = pedidoDAO.listarTodos();
        resumo.setTotalPedidos(pedidos.size());
        BigDecimal faturamento = BigDecimal.ZERO;
        for (Pedido p : pedidos) {
            resumo.getPedidosPorStatus().merge(p.getStatus(), 1, Integer::sum);
            if (p.getStatus() != StatusPedido.CANCELADO) {
                faturamento = faturamento.add(p.getValorTotal());
            }
            if (resumo.getUltimosPedidos().size() < QUANTIDADE_ULTIMOS_PEDIDOS) {
                resumo.getUltimosPedidos().add(p);
            }
        }
        resumo.setFaturamento(faturamento);

        // Produtos e alertas de estoque
        List<Produto> produtos = produtoDAO.listarTodos();
        resumo.setTotalProdutos(produtos.size());
        for (Produto p : produtos) {
            if (p.isEsgotado()) {
                resumo.getProdutosEsgotados().add(p);
            } else if (p.getEstoques().stream().anyMatch(Estoque::isEstoqueBaixo)) {
                resumo.getProdutosEstoqueBaixo().add(p);
            }
        }
        return resumo;
    }
}
