package br.com.fashionstore.filter;

import java.io.IOException;

import br.com.fashionstore.service.MenuCategorias;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Coloca na requisição as categorias do menu do cabeçalho
 * (${categoriasMenu}), para que todas as páginas mostrem o mesmo menu
 * sem cada Servlet precisar carregar as categorias.
 *
 * Arquivos estáticos (CSS, imagens, JS) são ignorados.
 */
@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.ERROR})
public class MenuFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!request.getServletPath().startsWith("/assets/")) {
            request.setAttribute("categoriasMenu", MenuCategorias.listar());
        }
        chain.doFilter(request, response);
    }
}
