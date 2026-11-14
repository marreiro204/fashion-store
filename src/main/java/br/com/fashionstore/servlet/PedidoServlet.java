package br.com.fashionstore.servlet;

import java.io.IOException;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Carrinho;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.StatusPedido;
import br.com.fashionstore.service.CarrinhoService;
import br.com.fashionstore.service.PedidoService;
import br.com.fashionstore.service.ValidacaoException;
import br.com.fashionstore.util.SessaoUtil;
import br.com.fashionstore.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Pedidos do cliente (precisa estar logado: ControleAcessoFilter).
 *
 *   GET  /pedido          -> finalização (resumo + escolha do endereço)  [pedido.jsp]
 *   POST /pedido          -> confirma a compra
 *   GET  /pedidos         -> histórico de compras                        [pedidos.jsp]
 *   GET  /pedidos?id=12   -> detalhes de um pedido do próprio cliente     [pedidos.jsp]
 *
 * Se o visitante não estiver logado ao clicar em "Finalizar compra", o
 * filtro manda para o login e depois volta para /pedido. O carrinho é
 * mantido, porque fica na sessão.
 *
 * Área administrativa (só ADMIN):
 *   GET  /admin/pedidos?status=PAGO  -> consulta de pedidos (filtro opcional)
 *   GET  /admin/pedidos?id=12        -> detalhes do pedido
 *   POST /admin/pedidos  id, status  -> atualiza o status
 */
@WebServlet(urlPatterns = {"/pedido", "/pedidos", "/admin/pedidos"})
public class PedidoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW_FINALIZAR = "/WEB-INF/views/pedido/pedido.jsp";
    private static final String VIEW_PEDIDOS = "/WEB-INF/views/pedido/pedidos.jsp";
    private static final String VIEW_ADMIN = "/WEB-INF/views/admin/pedidos-admin.jsp";
    private static final String ERRO_BANCO = "Não foi possível acessar o banco de dados agora. Tente novamente em instantes.";

    private final PedidoService pedidoService = new PedidoService();
    private final CarrinhoService carrinhoService = new CarrinhoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        switch (request.getServletPath()) {
            case "/pedido" -> mostrarFinalizacao(request, response);
            case "/admin/pedidos" -> adminPedidos(request, response);
            default -> mostrarPedidos(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("/admin/pedidos".equals(request.getServletPath())) {
            adminAlterarStatus(request, response);
            return;
        }
        if (!"/pedido".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        Cliente cliente = SessaoUtil.getUsuarioLogado(request);
        Carrinho carrinho = SessaoUtil.getCarrinho(request);
        Integer idEndereco = Validador.converterInteiro(request.getParameter("endereco"));
        request.setAttribute("enderecoSelecionado", idEndereco); // mantém a escolha se der erro

        try {
            Pedido pedido = pedidoService.finalizar(cliente, carrinho, idEndereco);
            SessaoUtil.mensagem(request, "Pedido nº " + pedido.getIdPedido()
                    + " realizado com sucesso! Acompanhe o status por aqui.");
            response.sendRedirect(request.getContextPath() + "/pedidos?id=" + pedido.getIdPedido());

        } catch (ValidacaoException e) {
            request.setAttribute("erros", e.getErros());
            mostrarFinalizacao(request, response);

        } catch (DAOException e) {
            log("Erro de banco ao finalizar pedido", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
            mostrarFinalizacao(request, response);
        }
    }

    // ===================== Área administrativa =====================

    /** Lista (com filtro de status) ou detalhes de um pedido. */
    private void adminPedidos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idTexto = request.getParameter("id");
        try {
            if (idTexto != null) {
                Integer id = Validador.converterInteiro(idTexto);
                Pedido pedido = id == null ? null : pedidoService.buscar(id);
                if (pedido == null) {
                    SessaoUtil.mensagem(request, "Pedido não encontrado.");
                    response.sendRedirect(request.getContextPath() + "/admin/pedidos");
                    return;
                }
                request.setAttribute("pedido", pedido);
            } else {
                StatusPedido filtro = StatusPedido.de(request.getParameter("status"));
                request.setAttribute("statusFiltro", filtro);
                request.setAttribute("pedidos", pedidoService.listarTodos(filtro == null ? null : filtro.name()));
            }
        } catch (DAOException e) {
            log("Erro de banco na consulta de pedidos", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.setAttribute("todosStatus", StatusPedido.values());
        request.getRequestDispatcher(VIEW_ADMIN).forward(request, response);
    }

    /** Atualiza o status e volta para os detalhes do pedido (PRG). */
    private void adminAlterarStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = Validador.converterInteiro(request.getParameter("id"));
        try {
            StatusPedido novo = pedidoService.alterarStatus(id, request.getParameter("status"));
            SessaoUtil.mensagem(request, "Pedido nº " + id + " atualizado para \"" + novo.getDescricao() + "\""
                    + (novo == StatusPedido.CANCELADO ? ". As peças voltaram para o estoque." : "."));
        } catch (ValidacaoException e) {
            SessaoUtil.mensagem(request, String.join(" ", e.getErros().values()));
        } catch (DAOException e) {
            log("Erro de banco ao atualizar status", e);
            SessaoUtil.mensagem(request, ERRO_BANCO);
        }
        response.sendRedirect(request.getContextPath() + "/admin/pedidos" + (id != null ? "?id=" + id : ""));
    }

    /** Página de finalização: resumo atualizado do carrinho + endereços. */
    private void mostrarFinalizacao(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Carrinho carrinho = SessaoUtil.getCarrinho(request);
        if (carrinho.isVazio() && request.getAttribute("erros") == null) {
            SessaoUtil.mensagem(request, "Seu carrinho está vazio.");
            response.sendRedirect(request.getContextPath() + "/carrinho");
            return;
        }

        Cliente cliente = SessaoUtil.getUsuarioLogado(request);
        try {
            request.setAttribute("avisos", carrinhoService.atualizar(carrinho));
            request.setAttribute("enderecos", pedidoService.listarEnderecos(cliente.getIdCliente()));
        } catch (DAOException e) {
            log("Erro de banco na finalização", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.setAttribute("carrinho", carrinho);
        request.getRequestDispatcher(VIEW_FINALIZAR).forward(request, response);
    }

    /** Histórico (sem id) ou detalhes de um pedido (com id). */
    private void mostrarPedidos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Cliente cliente = SessaoUtil.getUsuarioLogado(request);
        String idTexto = request.getParameter("id");

        try {
            if (idTexto != null) {
                Integer id = Validador.converterInteiro(idTexto);
                Pedido pedido = id == null ? null : pedidoService.buscarDoCliente(id, cliente.getIdCliente());
                if (pedido == null) {
                    // Não existe ou é de outro cliente: mesma resposta (não revela qual dos dois).
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("pedido", pedido);
            } else {
                request.setAttribute("pedidos", pedidoService.listarHistorico(cliente.getIdCliente()));
            }
        } catch (DAOException e) {
            log("Erro de banco nos pedidos", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.getRequestDispatcher(VIEW_PEDIDOS).forward(request, response);
    }
}
