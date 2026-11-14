package br.com.fashionstore.servlet;

import java.io.IOException;
import java.util.List;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Carrinho;
import br.com.fashionstore.service.CarrinhoService;
import br.com.fashionstore.service.ValidacaoException;
import br.com.fashionstore.util.SessaoUtil;
import br.com.fashionstore.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Carrinho de compras (público: não precisa de login para montar o carrinho).
 *
 *   GET  /carrinho                         -> mostra o carrinho
 *   POST /carrinho  acao=adicionar         -> produto, tamanho, quantidade
 *   POST /carrinho  acao=alterar           -> produto, tamanho, quantidade (0 remove)
 *   POST /carrinho  acao=remover           -> produto, tamanho
 *   POST /carrinho  acao=limpar            -> esvazia
 */
@WebServlet("/carrinho")
public class CarrinhoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/WEB-INF/views/carrinho/carrinho.jsp";

    private final CarrinhoService carrinhoService = new CarrinhoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Carrinho carrinho = SessaoUtil.getCarrinho(request);
        if (!carrinho.isVazio()) {
            try {
                // Confere preço e estoque atuais antes de mostrar.
                List<String> avisos = carrinhoService.atualizar(carrinho);
                request.setAttribute("avisos", avisos);
            } catch (DAOException e) {
                log("Erro de banco ao atualizar o carrinho", e);
                request.setAttribute("erroGeral",
                        "Não foi possível conferir preços e estoque agora. Os valores podem estar desatualizados.");
            }
        }
        request.setAttribute("carrinho", carrinho);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Carrinho carrinho = SessaoUtil.getCarrinho(request);
        String acao = request.getParameter("acao");
        Integer idProduto = Validador.converterInteiro(request.getParameter("produto"));
        String tamanho = request.getParameter("tamanho");
        String quantidade = request.getParameter("quantidade");
        String ctx = request.getContextPath();

        try {
            switch (acao == null ? "" : acao) {
                case "adicionar" -> {
                    carrinhoService.adicionar(carrinho, idProduto, tamanho, quantidade);
                    SessaoUtil.mensagem(request, "Produto adicionado ao carrinho.");
                }
                case "alterar" -> {
                    carrinhoService.alterarQuantidade(carrinho, idProduto, tamanho, quantidade);
                    SessaoUtil.mensagem(request, "Carrinho atualizado.");
                }
                case "remover" -> {
                    carrinhoService.remover(carrinho, idProduto, tamanho);
                    SessaoUtil.mensagem(request, "Item removido do carrinho.");
                }
                case "limpar" -> {
                    carrinho.limpar();
                    SessaoUtil.mensagem(request, "Carrinho esvaziado.");
                }
                default -> {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            }
            response.sendRedirect(ctx + "/carrinho");

        } catch (ValidacaoException e) {
            // Mostra o motivo e volta para onde o cliente estava.
            SessaoUtil.mensagem(request, String.join(" ", e.getErros().values()));
            if ("adicionar".equals(acao) && idProduto != null) {
                response.sendRedirect(ctx + "/produto?id=" + idProduto);
            } else {
                response.sendRedirect(ctx + "/carrinho");
            }

        } catch (DAOException e) {
            log("Erro de banco no carrinho", e);
            SessaoUtil.mensagem(request, "Não foi possível acessar o banco de dados agora. Tente novamente em instantes.");
            response.sendRedirect(ctx + ("adicionar".equals(acao) && idProduto != null
                    ? "/produto?id=" + idProduto : "/carrinho"));
        }
    }
}
