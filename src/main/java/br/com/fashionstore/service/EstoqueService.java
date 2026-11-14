package br.com.fashionstore.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.dao.EstoqueDAO;
import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.util.Validador;

/**
 * Regras do controle de estoque por tamanho.
 *
 * - Admin: cadastrar tamanhos, alterar quantidades, remover tamanhos;
 * - Carrinho (Etapa 7): verificar se há a quantidade pedida de um tamanho.
 *
 * A BAIXA do estoque ao finalizar o pedido não fica aqui: ela é feita pelo
 * PedidoDAO dentro da transação do pedido (Etapa 3), para nunca ficar
 * estoque baixado sem pedido ou pedido sem baixa.
 */
public class EstoqueService {

    /** Limite de segurança por tamanho, para evitar erros de digitação (ex.: 10000000). */
    public static final int QUANTIDADE_MAXIMA = 100_000;

    private final EstoqueDAO estoqueDAO;
    private final ProdutoDAO produtoDAO;

    public EstoqueService() {
        this(new EstoqueDAO(), new ProdutoDAO());
    }

    public EstoqueService(EstoqueDAO estoqueDAO, ProdutoDAO produtoDAO) {
        this.estoqueDAO = estoqueDAO;
        this.produtoDAO = produtoDAO;
    }

    /** Todos os produtos com seus tamanhos (visão geral do estoque). */
    public List<Produto> listarProdutosComEstoque() {
        return produtoDAO.listarTodos();
    }

    /** @return o produto com seus tamanhos, ou null se não existir */
    public Produto buscarProduto(int idProduto) {
        return produtoDAO.buscarPorId(idProduto);
    }

    /**
     * Cadastra um tamanho NOVO para o produto.
     *
     * @throws ValidacaoException tamanho/quantidade inválidos, produto
     *                            inexistente ou tamanho já cadastrado
     */
    public void adicionarTamanho(int idProduto, String tamanhoDigitado, String quantidadeDigitada)
            throws ValidacaoException {

        String tamanho = normalizarTamanho(tamanhoDigitado);
        Integer quantidade = Validador.converterInteiro(quantidadeDigitada);
        validar(idProduto, tamanho, quantidade);

        if (estoqueDAO.buscarPorProdutoETamanho(idProduto, tamanho) != null) {
            throw new ValidacaoException("tamanho",
                    "O tamanho " + tamanho + " já está cadastrado. Altere a quantidade na tabela.");
        }
        estoqueDAO.definirQuantidade(idProduto, tamanho, quantidade);
    }

    /**
     * Altera a quantidade de um tamanho já cadastrado (ex.: chegou mercadoria
     * ou foi feita uma contagem).
     */
    public void alterarQuantidade(int idProduto, String tamanhoDigitado, String quantidadeDigitada)
            throws ValidacaoException {

        String tamanho = normalizarTamanho(tamanhoDigitado);
        Integer quantidade = Validador.converterInteiro(quantidadeDigitada);
        validar(idProduto, tamanho, quantidade);

        if (estoqueDAO.buscarPorProdutoETamanho(idProduto, tamanho) == null) {
            throw new ValidacaoException("tamanho", "Tamanho " + tamanho + " não encontrado neste produto.");
        }
        estoqueDAO.definirQuantidade(idProduto, tamanho, quantidade);
    }

    /**
     * Remove um tamanho do produto.
     * Pedidos antigos não são afetados: o item do pedido guarda o tamanho como texto.
     *
     * @return o id do produto (para a tela voltar a ele)
     */
    public int removerTamanho(int idEstoque) throws ValidacaoException {
        Estoque estoque = estoqueDAO.buscarPorId(idEstoque);
        if (estoque == null) {
            throw new ValidacaoException("geral", "Tamanho não encontrado.");
        }
        estoqueDAO.excluir(idEstoque);
        return estoque.getIdProduto();
    }

    /**
     * Validação de disponibilidade, usada pelo carrinho (Etapa 7).
     *
     * @return a quantidade disponível do tamanho
     * @throws ValidacaoException com uma mensagem pronta para o cliente
     *                            (tamanho inexistente, esgotado ou insuficiente)
     */
    public int verificarDisponibilidade(int idProduto, String tamanho, int quantidadeDesejada)
            throws ValidacaoException {

        if (quantidadeDesejada <= 0) {
            throw new ValidacaoException("quantidade", "A quantidade deve ser maior que zero.");
        }
        Estoque estoque = Validador.vazio(tamanho) ? null
                : estoqueDAO.buscarPorProdutoETamanho(idProduto, normalizarTamanho(tamanho));

        if (estoque == null) {
            throw new ValidacaoException("tamanho", "Selecione um tamanho disponível.");
        }
        if (estoque.getQuantidade() == 0) {
            throw new ValidacaoException("tamanho", "O tamanho " + estoque.getTamanho() + " está esgotado.");
        }
        if (!estoque.temDisponivel(quantidadeDesejada)) {
            throw new ValidacaoException("quantidade", "Só temos " + estoque.getQuantidade()
                    + " unidade(s) do tamanho " + estoque.getTamanho() + ".");
        }
        return estoque.getQuantidade();
    }

    // ========================= Auxiliares =========================

    /** " m " -> "M"; "gg" -> "GG". Tamanhos ficam sempre em maiúsculas. */
    public static String normalizarTamanho(String tamanho) {
        return Validador.limpar(tamanho).toUpperCase();
    }

    private void validar(int idProduto, String tamanho, Integer quantidade) throws ValidacaoException {
        Map<String, String> erros = new LinkedHashMap<>();

        // Letras e números, sem espaços: P, GG, 38, UNICO...
        if (!tamanho.matches("[\\p{L}\\p{N}]{1,10}")) {
            erros.put("tamanho", "Informe o tamanho com até 10 letras ou números (ex.: P, GG, 40).");
        }
        if (quantidade == null || quantidade < 0) {
            erros.put("quantidade", "Informe uma quantidade inteira, zero ou maior.");
        } else if (quantidade > QUANTIDADE_MAXIMA) {
            erros.put("quantidade", "Quantidade acima do limite (" + QUANTIDADE_MAXIMA + ").");
        }
        if (erros.isEmpty() && produtoDAO.buscarPorId(idProduto) == null) {
            erros.put("geral", "Produto não encontrado.");
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }
}
