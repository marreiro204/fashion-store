package br.com.fashionstore.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.util.SessaoUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controle de acesso por perfil.
 *
 * Um Filter roda ANTES do Servlet. Ele decide se a requisição continua
 * (chain.doFilter) ou é barrada.
 *
 *   Área do cliente (precisa estar logado): /perfil, /pedidos, /pedido
 *   Área administrativa (precisa ser ADMIN): /admin e tudo em /admin/...
 *
 * Regras:
 * - não logado      -> vai para o login e, depois, volta para a página pedida;
 * - logado sem ADMIN tentando /admin -> erro 403 (acesso negado).
 *
 * Para proteger uma página nova, basta incluir o caminho em urlPatterns.
 */
@WebFilter(urlPatterns = {"/perfil", "/pedidos", "/pedido", "/admin/*"})
public class ControleAcessoFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Cliente usuario = SessaoUtil.getUsuarioLogado(request);

        if (usuario == null) {
            redirecionarParaLogin(request, response);
            return;
        }

        String caminho = request.getServletPath();
        if (caminho.startsWith("/admin") && !usuario.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Páginas com dados pessoais não ficam no cache do navegador:
        // depois do logout, o botão "Voltar" não mostra a página antiga.
        response.setHeader("Cache-Control", "no-store");

        chain.doFilter(request, response); // acesso liberado
    }

    private void redirecionarParaLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String login = request.getContextPath() + "/login";

        // Em GET, guardamos a página pedida para voltar a ela após o login.
        if ("GET".equals(request.getMethod())) {
            String destino = request.getServletPath()
                    + (request.getPathInfo() != null ? request.getPathInfo() : "")
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            login += "?redirect=" + URLEncoder.encode(destino, StandardCharsets.UTF_8);
        }

        SessaoUtil.mensagem(request, "Entre na sua conta para continuar.");
        response.sendRedirect(login);
    }
}
