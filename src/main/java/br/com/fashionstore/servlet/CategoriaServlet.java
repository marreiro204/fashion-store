package br.com.fashionstore.servlet;

import java.io.IOException;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.service.CategoriaService;
import br.com.fashionstore.service.ValidacaoException;
import br.com.fashionstore.util.SessaoUtil;
import br.com.fashionstore.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gerenciamento de categorias (só ADMIN).
 *
 *   GET  /admin/categorias                   -> lista + formulário de nova categoria
 *   GET  /admin/categorias?acao=editar&id=3  -> formulário preenchido
 *   POST /admin/categorias  acao=salvar      -> cadastra ou altera
 *   POST /admin/categorias  acao=excluir     -> exclui
 */
@WebServlet("/admin/categorias")
public class CategoriaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/WEB-INF/views/admin/categorias-admin.jsp";
    private static final String ERRO_BANCO = "Não foi possível acessar o banco de dados agora. Tente novamente em instantes.";

    private final CategoriaService categoriaService = new CategoriaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            if ("editar".equals(request.getParameter("acao"))) {
                Integer id = Validador.converterInteiro(request.getParameter("id"));
                Categoria categoria = id == null ? null : categoriaService.buscar(id);
                if (categoria == null) {
                    SessaoUtil.mensagem(request, "Categoria não encontrada.");
                    response.sendRedirect(request.getContextPath() + "/admin/categorias");
                    return;
                }
                request.setAttribute("categoria", categoria);
            }
            request.setAttribute("categorias", categoriaService.listar());
        } catch (DAOException e) {
            log("Erro de banco no admin de categorias", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String acao = request.getParameter("acao");
        if ("salvar".equals(acao)) {
            salvar(request, response);
        } else if ("excluir".equals(acao)) {
            excluir(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void salvar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = Validador.converterInteiro(request.getParameter("id"));
        Categoria categoria = new Categoria(id == null ? 0 : id,
                request.getParameter("nome"), request.getParameter("descricao"));
        boolean nova = categoria.getIdCategoria() == 0;

        try {
            categoriaService.salvar(categoria);
            SessaoUtil.mensagem(request, "Categoria \"" + categoria.getNome() + "\" "
                    + (nova ? "cadastrada." : "alterada."));
            response.sendRedirect(request.getContextPath() + "/admin/categorias");
            return;

        } catch (ValidacaoException e) {
            request.setAttribute("erros", e.getErros());
        } catch (DAOException e) {
            log("Erro de banco ao salvar categoria", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }

        // Houve erro: reexibe a página com o formulário e as mensagens.
        try {
            request.setAttribute("categorias", categoriaService.listar());
        } catch (DAOException e) {
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void excluir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = Validador.converterInteiro(request.getParameter("id"));
        try {
            if (id == null) {
                throw new ValidacaoException("geral", "Categoria não encontrada.");
            }
            categoriaService.excluir(id);
            SessaoUtil.mensagem(request, "Categoria excluída.");
        } catch (ValidacaoException e) {
            SessaoUtil.mensagem(request, e.getErros().get("geral"));
        } catch (DAOException e) {
            log("Erro de banco ao excluir categoria", e);
            SessaoUtil.mensagem(request, ERRO_BANCO);
        }
        response.sendRedirect(request.getContextPath() + "/admin/categorias");
    }
}
