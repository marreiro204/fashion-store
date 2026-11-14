package br.com.fashionstore.service;

import java.util.List;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.dao.EnderecoDAO;
import br.com.fashionstore.dao.PedidoDAO;
import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Carrinho;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.model.ItemPedido;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.model.StatusPedido;

/**
 * Regras dos pedidos do cliente: finalizar a compra e consultar o histórico.
 */
public class PedidoService {

    private final PedidoDAO pedidoDAO;
    private final ProdutoDAO produtoDAO;
    private final EnderecoDAO enderecoDAO;

    public PedidoService() {
        this(new PedidoDAO(), new ProdutoDAO(), new EnderecoDAO());
    }

    public PedidoService(PedidoDAO pedidoDAO, ProdutoDAO produtoDAO, EnderecoDAO enderecoDAO) {
        this.pedidoDAO = pedidoDAO;
        this.produtoDAO = produtoDAO;
        this.enderecoDAO = enderecoDAO;
    }

    /** Endereços do cliente, para escolher o de entrega. */
    public List<Endereco> listarEnderecos(int idCliente) {
        return enderecoDAO.listarPorCliente(idCliente);
    }

    /**
     * Finaliza a compra: transforma o carrinho em um pedido.
     *
     * 1. Confere carrinho e endereço (o endereço precisa ser DO cliente);
     * 2. Recarrega cada produto: o preço gravado é o preço ATUAL
     *    (nunca o que veio do navegador);
     * 3. Confere o estoque e mostra uma mensagem clara se faltar algo;
     * 4. Grava pedido + itens + baixa no estoque em UMA transação (PedidoDAO);
     * 5. Esvazia o carrinho.
     *
     * @return o pedido gravado (com id)
     * @throws ValidacaoException se algo impedir a compra (nada é gravado)
     */
    public Pedido finalizar(Cliente cliente, Carrinho carrinho, Integer idEndereco) throws ValidacaoException {
        if (carrinho == null || carrinho.isVazio()) {
            throw new ValidacaoException("geral", "Seu carrinho está vazio.");
        }

        Endereco endereco = idEndereco == null ? null : enderecoDAO.buscarPorId(idEndereco);
        if (endereco == null || endereco.getIdCliente() != cliente.getIdCliente()) {
            throw new ValidacaoException("endereco", "Selecione um endereço de entrega.");
        }

        Pedido pedido = new Pedido(cliente, endereco);
        for (ItemPedido itemCarrinho : carrinho.getItens()) {
            Produto atual = produtoDAO.buscarPorId(itemCarrinho.getProduto().getIdProduto());
            if (atual == null) {
                throw new ValidacaoException("geral", itemCarrinho.getProduto().getNome()
                        + " não está mais disponível. Remova-o do carrinho para continuar.");
            }
            int disponivel = atual.getQuantidadeDisponivel(itemCarrinho.getTamanho());
            if (disponivel < itemCarrinho.getQuantidade()) {
                throw new ValidacaoException("geral", disponivel == 0
                        ? atual.getNome() + " (tamanho " + itemCarrinho.getTamanho() + ") esgotou. Ajuste o carrinho."
                        : "Só temos " + disponivel + " unidade(s) de " + atual.getNome()
                          + " (tamanho " + itemCarrinho.getTamanho() + "). Ajuste o carrinho.");
            }
            pedido.adicionarItem(new ItemPedido(atual, itemCarrinho.getTamanho(), itemCarrinho.getQuantidade()));
        }

        try {
            pedidoDAO.salvar(pedido);
        } catch (DAOException e) {
            // Outra pessoa comprou a última peça entre a conferência e a gravação.
            if (e.getMotivo() == DAOException.Motivo.ESTOQUE_INSUFICIENTE) {
                throw new ValidacaoException("geral",
                        "Um dos produtos acabou de esgotar. Confira o carrinho e tente novamente.");
            }
            throw e;
        }

        carrinho.limpar();
        return pedido;
    }

    // ===================== Área administrativa =====================

    /**
     * Todos os pedidos, ou só os de um status (filtro da tela do admin).
     * Status inválido ou vazio = todos.
     */
    public List<Pedido> listarTodos(String statusTexto) {
        StatusPedido status = StatusPedido.de(statusTexto);
        return status == null ? pedidoDAO.listarTodos() : pedidoDAO.listarPorStatus(status);
    }

    /** Qualquer pedido (admin), com itens. @return null se não existir */
    public Pedido buscar(int idPedido) {
        return pedidoDAO.buscarPorId(idPedido);
    }

    /**
     * Muda o status de um pedido, respeitando o caminho permitido
     * (ver StatusPedido). Ao CANCELAR, o PedidoDAO devolve as peças ao
     * estoque na mesma transação.
     *
     * @return o novo status
     */
    public StatusPedido alterarStatus(Integer idPedido, String novoStatusTexto) throws ValidacaoException {
        StatusPedido novo = StatusPedido.de(novoStatusTexto);
        if (novo == null) {
            throw new ValidacaoException("status", "Selecione um status válido.");
        }
        Pedido pedido = idPedido == null ? null : pedidoDAO.buscarPorId(idPedido);
        if (pedido == null) {
            throw new ValidacaoException("geral", "Pedido não encontrado.");
        }
        StatusPedido atual = pedido.getStatus();
        if (!atual.podeMudarPara(novo)) {
            throw new ValidacaoException("status", atual.isFinal()
                    ? "Este pedido já está " + atual.getDescricao().toLowerCase() + " e não pode mais ser alterado."
                    : "Não é possível mudar de " + atual.getDescricao() + " para " + novo.getDescricao() + ".");
        }
        pedidoDAO.atualizarStatus(idPedido, novo);
        return novo;
    }

    /** Histórico do cliente (mais recentes primeiro). */
    public List<Pedido> listarHistorico(int idCliente) {
        return pedidoDAO.listarPorCliente(idCliente);
    }

    /**
     * Detalhes de um pedido, SOMENTE se ele for do cliente informado.
     * Assim um cliente não vê o pedido de outro trocando o número na URL.
     *
     * @return o pedido, ou null se não existir ou for de outra pessoa
     */
    public Pedido buscarDoCliente(int idPedido, int idCliente) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido);
        if (pedido == null || pedido.getCliente().getIdCliente() != idCliente) {
            return null;
        }
        return pedido;
    }
}
