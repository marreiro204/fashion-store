package br.com.fashionstore.servlet;

import java.io.IOException;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.service.EstoqueService;
import br.com.fashionstore.service.ValidacaoException;
import br.com.fashionstore.util.SessaoUtil;
import br.com.fashionstore.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controle de estoque por tamanho (só ADMIN).
 *
 *   GET  /admin/estoque                 -> visão geral: todos os produtos e seus tamanhos
 *   GET  /admin/estoque?produto=5       -> gerenciar os tamanhos do produto 5
 *   POST /admin/estoque  acao=adicionar -> novo tamanho (produto, tamanho, quantidade)
 *   POST /admin/estoque  acao=alterar   -> nova quantidade de um tamanho
 *   POST /admin/estoque  acao=remover   -> remove um tamanho (idEstoque)
 */
@WebServlet("/admin/estoque")
public class EstoqueServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/WEB-INF/views/admin/estoque-admin.jsp";
    private static final String ERRO_BANCO = "Não foi possível acessar o banco de dados agora. Tente novamente em instantes.";

    private final EstoqueService estoqueService = new EstoqueService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer idProduto = Validador.converterInteiro(request.getParameter("produto"));
        try {
            carregarPagina(request, idProduto);
            if (idProduto != null && request.getAttribute("produto") == null) {
                SessaoUtil.mensagem(request, "Produto não encontrado.");
                response.sendRedirect(request.getContextPath() + "/admin/estoque");
                return;
            }
        } catch (DAOException e) {
            log("Erro de banco no estoque", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String acao = request.getParameter("acao");
        Integer idProduto = Validador.converterInteiro(request.getParameter("produto"));
        String tamanho = request.getParameter("tamanho");
        String quantidade = request.getParameter("quantidade");

        try {
            switch (acao == null ? "" : acao) {
                case "adicionar" -> {
                    exigirProduto(idProduto);
                    estoqueService.adicionarTamanho(idProduto, tamanho, quantidade);
                    SessaoUtil.mensagem(request, "Tamanho " + EstoqueService.normalizarTamanho(tamanho)
                            + " cadastrado com " + quantidade.trim() + " unidade(s).");
                }
                case "alterar" -> {
                    exigirProduto(idProduto);
                    estoqueService.alterarQuantidade(idProduto, tamanho, quantidade);
                    SessaoUtil.mensagem(request, "Estoque do tamanho " + EstoqueService.normalizarTamanho(tamanho)
                            + " atualizado para " + quantidade.trim() + ".");
                }
                case "remover" -> {
                    Integer idEstoque = Validador.converterInteiro(request.getParameter("idEstoque"));
                    if (idEstoque == null) {
                        throw new ValidacaoException("geral", "Tamanho não encontrado.");
                    }
                    idProduto = estoqueService.removerTamanho(idEstoque);
                    SessaoUtil.mensagem(request, "Tamanho removido.");
                }
                default -> {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
            // Sucesso: PRG (redireciona para o mesmo produto)
            response.sendRedirect(request.getContextPath() + "/admin/estoque"
                    + (idProduto != null ? "?produto=" + idProduto : ""));

        } catch (ValidacaoException e) {
            // Erro: reexibe a página do produto com as mensagens (sem perder o que foi digitado)
            request.setAttribute("erros", e.getErros());
            request.setAttribute("acaoComErro", acao);
            reexibir(request, response, idProduto);

        } catch (DAOException e) {
            log("Erro de banco ao alterar estoque", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
            reexibir(request, response, idProduto);
        }
    }

    /** Coloca na requisição o produto escolhido (se houver) e a lista geral. */
    private void carregarPagina(HttpServletRequest request, Integer idProduto) {
        if (idProduto != null) {
            Produto produto = estoqueService.buscarProduto(idProduto);
            request.setAttribute("produto", produto);
        } else {
            request.setAttribute("produtos", estoqueService.listarProdutosComEstoque());
        }
    }

    private void reexibir(HttpServletRequest request, HttpServletResponse response, Integer idProduto)
            throws ServletException, IOException {
        try {
            carregarPagina(request, idProduto);
        } catch (DAOException e) {
            log("Erro de banco ao recarregar estoque", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void exigirProduto(Integer idProduto) throws ValidacaoException {
        if (idProduto == null) {
            throw new ValidacaoException("geral", "Produto não encontrado.");
        }
    }
}
