package br.com.fashionstore.service;

import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Carrinho;
import br.com.fashionstore.model.ItemPedido;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.util.Validador;

/**
 * Regras do carrinho: adicionar, alterar quantidade, remover e manter os
 * dados atualizados (preço e estoque podem mudar enquanto o produto está
 * no carrinho).
 *
 * O carrinho só CONFERE o estoque. A baixa acontece ao finalizar o pedido
 * (PedidoService -> PedidoDAO, em transação).
 */
public class CarrinhoService {

    private final ProdutoDAO produtoDAO;
    private final EstoqueService estoqueService;

    public CarrinhoService() {
        this(new ProdutoDAO(), new EstoqueService());
    }

    public CarrinhoService(ProdutoDAO produtoDAO, EstoqueService estoqueService) {
        this.produtoDAO = produtoDAO;
        this.estoqueService = estoqueService;
    }

    /**
     * Adiciona um produto no tamanho escolhido.
     *
     * @throws ValidacaoException produto inexistente, tamanho não escolhido,
     *                            esgotado, quantidade inválida ou acima do estoque
     */
    public void adicionar(Carrinho carrinho, Integer idProduto, String tamanhoDigitado, String quantidadeTexto)
            throws ValidacaoException {

        Integer quantidade = Validador.converterInteiro(quantidadeTexto);
        if (quantidade == null || quantidade < 1) {
            throw new ValidacaoException("quantidade", "Informe uma quantidade a partir de 1.");
        }
        Produto produto = idProduto == null ? null : produtoDAO.buscarPorId(idProduto);
        if (produto == null) {
            throw new ValidacaoException("geral", "Produto não encontrado.");
        }
        if (Validador.vazio(tamanhoDigitado)) {
            throw new ValidacaoException("tamanho", "Selecione um tamanho.");
        }
        String tamanho = EstoqueService.normalizarTamanho(tamanhoDigitado);

        // Soma com o que já está no carrinho, para não ultrapassar o estoque em duas adições.
        ItemPedido existente = carrinho.buscarItem(produto.getIdProduto(), tamanho);
        int total = quantidade + (existente == null ? 0 : existente.getQuantidade());
        if (total > Carrinho.MAXIMO_POR_ITEM) {
            throw new ValidacaoException("quantidade",
                    "Máximo de " + Carrinho.MAXIMO_POR_ITEM + " unidades por produto e tamanho"
                    + (existente != null ? " (você já tem " + existente.getQuantidade() + " no carrinho)." : "."));
        }
        estoqueService.verificarDisponibilidade(produto.getIdProduto(), tamanho, total);

        carrinho.adicionar(produto, tamanho, quantidade);
    }

    /**
     * Altera a quantidade de um item. Quantidade 0 remove o item.
     */
    public void alterarQuantidade(Carrinho carrinho, Integer idProduto, String tamanhoDigitado,
                                  String quantidadeTexto) throws ValidacaoException {

        String tamanho = EstoqueService.normalizarTamanho(tamanhoDigitado);
        if (idProduto == null || carrinho.buscarItem(idProduto, tamanho) == null) {
            throw new ValidacaoException("geral", "Item não encontrado no carrinho.");
        }
        Integer quantidade = Validador.converterInteiro(quantidadeTexto);
        if (quantidade == null || quantidade < 0) {
            throw new ValidacaoException("quantidade", "Informe uma quantidade válida.");
        }
        if (quantidade == 0) {
            carrinho.remover(idProduto, tamanho);
            return;
        }
        if (quantidade > Carrinho.MAXIMO_POR_ITEM) {
            throw new ValidacaoException("quantidade",
                    "Máximo de " + Carrinho.MAXIMO_POR_ITEM + " unidades por produto e tamanho.");
        }
        estoqueService.verificarDisponibilidade(idProduto, tamanho, quantidade);
        carrinho.alterarQuantidade(idProduto, tamanho, quantidade);
    }

    public void remover(Carrinho carrinho, Integer idProduto, String tamanho) {
        if (idProduto != null) {
            carrinho.remover(idProduto, EstoqueService.normalizarTamanho(tamanho));
        }
    }

    /**
     * Confere cada item com o banco: produto excluído, preço alterado ou
     * estoque menor que o pedido. Ajusta o carrinho e devolve avisos para
     * mostrar ao cliente (lista vazia = nada mudou).
     */
    public List<String> atualizar(Carrinho carrinho) {
        List<String> avisos = new ArrayList<>();

        for (ItemPedido item : new ArrayList<>(carrinho.getItens())) {
            int idProduto = item.getProduto().getIdProduto();
            String nome = item.getProduto().getNome();
            Produto atual = produtoDAO.buscarPorId(idProduto);

            if (atual == null) {
                carrinho.remover(idProduto, item.getTamanho());
                avisos.add(nome + " não está mais disponível e foi removido do carrinho.");
                continue;
            }

            int disponivel = atual.getQuantidadeDisponivel(item.getTamanho());
            if (disponivel == 0) {
                carrinho.remover(idProduto, item.getTamanho());
                avisos.add(atual.getNome() + " (tamanho " + item.getTamanho() + ") esgotou e foi removido do carrinho.");
                continue;
            }

            item.setProduto(atual);
            if (item.getPrecoUnitario().compareTo(atual.getPreco()) != 0) {
                item.setPrecoUnitario(atual.getPreco()); // recalcula o subtotal
                avisos.add("O preço de " + atual.getNome() + " foi atualizado.");
            }
            if (item.getQuantidade() > disponivel) {
                item.setQuantidade(disponivel);
                avisos.add("Só temos " + disponivel + " unidade(s) de " + atual.getNome()
                        + " (tamanho " + item.getTamanho() + "). A quantidade foi ajustada.");
            }
        }
        return avisos;
    }
}
