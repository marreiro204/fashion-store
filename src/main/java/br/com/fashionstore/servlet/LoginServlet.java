package br.com.fashionstore.servlet;

import java.io.IOException;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.service.ClienteService;
import br.com.fashionstore.util.SessaoUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Login e logout.
 *
 *   GET  /login   -> mostra o formulário
 *   POST /login   -> confere e-mail e senha
 *   POST /logout  -> encerra a sessão
 *
 * O logout é POST (um botão de formulário) e não um link GET: assim um
 * site malicioso não consegue deslogar o usuário só incluindo uma imagem
 * com src=".../logout".
 */
@WebServlet(urlPatterns = {"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW_LOGIN = "/WEB-INF/views/cliente/login.jsp";

    private final ClienteService clienteService = new ClienteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("/logout".equals(request.getServletPath())) {
            // Logout só por POST; digitar /logout na barra só volta ao início.
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        if (SessaoUtil.getUsuarioLogado(request) != null) {
            response.sendRedirect(request.getContextPath() + "/"); // já está logado
            return;
        }
        request.getRequestDispatcher(VIEW_LOGIN).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("/logout".equals(request.getServletPath())) {
            SessaoUtil.logout(request);
            SessaoUtil.mensagem(request, "Você saiu da sua conta.");
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        Cliente cliente;
        try {
            cliente = clienteService.autenticar(email, senha);
        } catch (DAOException e) {
            log("Erro de banco no login", e); // detalhe técnico vai para o log do Tomcat
            request.setAttribute("erro",
                    "Não foi possível acessar o banco de dados agora. Tente novamente em instantes.");
            request.getRequestDispatcher(VIEW_LOGIN).forward(request, response);
            return;
        }

        if (cliente == null) {
            request.setAttribute("erro", "E-mail ou senha inválidos.");
            request.getRequestDispatcher(VIEW_LOGIN).forward(request, response);
            return;
        }

        SessaoUtil.login(request, cliente);
        SessaoUtil.mensagem(request, "Olá, " + cliente.getNome() + "! Login realizado.");

        // PRG (Post/Redirect/Get): depois de um POST bem-sucedido, redirecionamos.
        // Assim, atualizar a página (F5) não reenvia o formulário.
        response.sendRedirect(request.getContextPath() + destinoAposLogin(request, cliente));
    }

    /**
     * Para onde ir depois do login:
     * - a página que o usuário tentou abrir antes (parâmetro "redirect"),
     *   desde que seja um caminho INTERNO do site;
     * - senão, o painel (admin) ou a página inicial (cliente).
     */
    private String destinoAposLogin(HttpServletRequest request, Cliente cliente) {
        String destino = request.getParameter("redirect");
        // Aceita só "/algo" — recusa "//site.com" e "https://...", que levariam para outro site.
        if (destino != null && destino.startsWith("/") && !destino.startsWith("//")
                && !destino.contains("\\") && !destino.contains("\r") && !destino.contains("\n")) {
            return destino;
        }
        return cliente.isAdmin() ? "/admin" : "/";
    }
}
