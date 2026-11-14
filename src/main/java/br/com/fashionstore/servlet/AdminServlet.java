package br.com.fashionstore.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Painel administrativo (admin.jsp).
 *
 * Não está na lista de Servlets do PDF, mas é necessário: como as JSPs
 * ficam em WEB-INF (não podem ser abertas direto), toda página precisa
 * de um Servlet que a encaminhe. O acesso é liberado só para ADMIN pelo
 * ControleAcessoFilter. Na Etapa 8 o painel ganha os resumos da loja.
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/admin/admin.jsp").forward(request, response);
    }
}
