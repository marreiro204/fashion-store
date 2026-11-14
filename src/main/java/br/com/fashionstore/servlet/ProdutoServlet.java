package br.com.fashionstore.servlet;

import java.io.IOException;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.service.CategoriaService;
import br.com.fashionstore.service.ProdutoService;
import br.com.fashionstore.service.ValidacaoException;
import br.com.fashionstore.util.SessaoUtil;
import br.com.fashionstore.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Produtos: catálogo público e gerenciamento (admin).
 *
 * Área do cliente (pública):
 *   GET /produtos?busca=jeans&categoria=2 -> catálogo com pesquisa e filtro
 *   GET /produto?id=5                     -> detalhes do produto
 *
 * Área administrativa (só ADMIN, garantido pelo ControleAcessoFilter):
 *   GET  /admin/produtos                    -> lista
 *   GET  /admin/produtos?acao=novo          -> formulário vazio
 *   GET  /admin/produtos?acao=editar&id=5   -> formulário preenchido
 *   POST /admin/produtos  acao=salvar       -> cadastra ou altera
 *   POST /admin/produtos  acao=excluir      -> exclui
 */
@WebServlet(urlPatterns = {"/produtos", "/produto", "/admin/produtos"})
public class ProdutoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VIEW_CATALOGO = "/WEB-INF/views/produto/produtos.jsp";
    private static final String VIEW_DETALHE = "/WEB-INF/views/produto/produto.jsp";
    private static final String VIEW_ADMIN = "/WEB-INF/views/admin/produtos-admin.jsp";
    private static final String ERRO_BANCO = "Não foi possível acessar o banco de dados agora. Tente novamente em instantes.";

    private final ProdutoService produtoService = new ProdutoService();
    private final CategoriaService categoriaService = new CategoriaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        switch (request.getServletPath()) {
            case "/produtos" -> catalogo(request, response);
            case "/produto" -> detalhe(request, response);
            default -> adminListar(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"/admin/produtos".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        String acao = request.getParameter("acao");
        if ("salvar".equals(acao)) {
            adminSalvar(request, response);
        } else if ("excluir".equals(acao)) {
            adminExcluir(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // ========================= Área do cliente =========================

    private void catalogo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String busca = request.getParameter("busca");
        Integer idCategoria = Validador.converterInteiro(request.getParameter("categoria"));
        request.setAttribute("categoriaSelecionada", idCategoria);
        try {
            request.setAttribute("produtos", produtoService.listarCatalogo(busca, idCategoria));
            request.setAttribute("categorias", categoriaService.listar());
        } catch (DAOException e) {
            log("Erro de banco no catálogo", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.getRequestDispatcher(VIEW_CATALOGO).forward(request, response);
    }

    private void detalhe(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = Validador.converterInteiro(request.getParameter("id"));
        if (id == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            Produto produto = produtoService.buscar(id);
            if (produto == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("produto", produto);
        } catch (DAOException e) {
            log("Erro de banco no detalhe do produto", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.getRequestDispatcher(VIEW_DETALHE).forward(request, response);
    }

    // ========================= Área administrativa =========================

    private void adminListar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String acao = request.getParameter("acao");
        try {
            if ("editar".equals(acao)) {
                Integer id = Validador.converterInteiro(request.getParameter("id"));
                Produto produto = id == null ? null : produtoService.buscar(id);
                if (produto == null) {
                    SessaoUtil.mensagem(request, "Produto não encontrado.");
                    response.sendRedirect(request.getContextPath() + "/admin/produtos");
                    return;
                }
                request.setAttribute("produto", produto);
                request.setAttribute("categoriaSelecionada", produto.getCategoria().getIdCategoria());
                request.setAttribute("mostrarFormulario", true);
            } else if ("novo".equals(acao)) {
                request.setAttribute("mostrarFormulario", true);
            }
            carregarListas(request);
        } catch (DAOException e) {
            log("Erro de banco no admin de produtos", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
        request.getRequestDispatcher(VIEW_ADMIN).forward(request, response);
    }

    private void adminSalvar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Produto produto = new Produto();
        Integer id = Validador.converterInteiro(request.getParameter("id"));
        produto.setIdProduto(id == null ? 0 : id);
        produto.setNome(request.getParameter("nome"));
        produto.setDescricao(request.getParameter("descricao"));
        produto.setImagem(request.getParameter("imagem"));
        // Se houver erro, o formulário volta com a categoria escolhida marcada.
        request.setAttribute("categoriaSelecionada", Validador.converterInteiro(request.getParameter("categoria")));

        try {
            boolean novo = produto.getIdProduto() == 0;
            produtoService.salvar(produto, request.getParameter("preco"),
                    Validador.converterInteiro(request.getParameter("categoria")));

            if (novo) {
                // Produto novo ainda não tem tamanhos: leva direto para a tela de estoque dele.
                SessaoUtil.mensagem(request, "Produto \"" + produto.getNome()
                        + "\" cadastrado. Agora cadastre os tamanhos e as quantidades.");
                response.sendRedirect(request.getContextPath() + "/admin/estoque?produto=" + produto.getIdProduto());
            } else {
                SessaoUtil.mensagem(request, "Produto \"" + produto.getNome() + "\" alterado.");
                response.sendRedirect(request.getContextPath() + "/admin/produtos");
            }

        } catch (ValidacaoException e) {
            request.setAttribute("erros", e.getErros());
            request.setAttribute("mostrarFormulario", true);
            carregarListasSemFalhar(request);
            request.getRequestDispatcher(VIEW_ADMIN).forward(request, response);

        } catch (DAOException e) {
            log("Erro de banco ao salvar produto", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
            request.setAttribute("mostrarFormulario", true);
            carregarListasSemFalhar(request);
            request.getRequestDispatcher(VIEW_ADMIN).forward(request, response);
        }
    }

    private void adminExcluir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = Validador.converterInteiro(request.getParameter("id"));
        try {
            if (id == null) {
                throw new ValidacaoException("geral", "Produto não encontrado.");
            }
            produtoService.excluir(id);
            SessaoUtil.mensagem(request, "Produto excluído.");
        } catch (ValidacaoException e) {
            SessaoUtil.mensagem(request, e.getErros().get("geral"));
        } catch (DAOException e) {
            log("Erro de banco ao excluir produto", e);
            SessaoUtil.mensagem(request, ERRO_BANCO);
        }
        response.sendRedirect(request.getContextPath() + "/admin/produtos");
    }

    private void carregarListas(HttpServletRequest request) {
        request.setAttribute("produtos", produtoService.listarCatalogo(null, null));
        request.setAttribute("categorias", categoriaService.listar());
    }

    /** Usado ao reexibir o formulário com erros: se o banco falhar, mostra a página mesmo assim. */
    private void carregarListasSemFalhar(HttpServletRequest request) {
        try {
            carregarListas(request);
        } catch (DAOException e) {
            log("Erro de banco ao recarregar listas", e);
            request.setAttribute("erroGeral", ERRO_BANCO);
        }
    }
}
