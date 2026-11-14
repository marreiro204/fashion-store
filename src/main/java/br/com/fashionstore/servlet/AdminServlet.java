package br.com.fashionstore.servlet;

import java.io.IOException;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.service.PainelService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Painel administrativo (admin.jsp) com o resumo da loja.
 *
 * Não está na lista de Servlets do PDF, mas é necessário: como as JSPs
 * ficam em WEB-INF (não podem ser abertas direto), toda página precisa
 * de um Servlet que a encaminhe. O acesso é liberado só para ADMIN pelo
 * ControleAcessoFilter.
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PainelService painelService = new PainelService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("resumo", painelService.gerarResumo());
        } catch (DAOException e) {
            log("Erro de banco no painel", e);
            request.setAttribute("erroGeral",
                    "Não foi possível carregar o resumo da loja agora. Os atalhos abaixo continuam disponíveis.");
        }
        request.getRequestDispatcher("/WEB-INF/views/admin/admin.jsp").forward(request, response);
    }
}
