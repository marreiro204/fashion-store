package br.com.fashionstore.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.service.CategoriaService;
import br.com.fashionstore.service.ProdutoService;
import br.com.fashionstore.util.Validador;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Catálogo (público, sem login).
 *
 *   GET /api/categorias                                 -> lista de categorias
 *   GET /api/produtos?busca=jeans&categoria=2&ordem=... -> lista de produtos
 *   GET /api/produtos/5                                 -> um produto (404 se não existir)
 *
 * ordem: nome (padrão), novidades, menor-preco, maior-preco
 */
@WebServlet(urlPatterns = {"/api/categorias", "/api/produtos/*"})   // "/api/produtos/*" também atende "/api/produtos"
public class ApiCatalogoServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    private final CategoriaService categoriaService = new CategoriaService();
    private final ProdutoService produtoService = new ProdutoService();

    @Override
    protected Object get(HttpServletRequest request, HttpServletResponse response) {
        ApiConversor conversor = new ApiConversor(request);

        if ("/api/categorias".equals(request.getServletPath())) {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Categoria c : categoriaService.listar()) {
                lista.add(conversor.categoria(c));
            }
            return lista;
        }

        Integer id = idDaUrl(request);
        if (id != null) {
            Produto produto = produtoService.buscar(id);
            if (produto == null) {
                throw new ApiErro(404, "Produto não encontrado.");
            }
            return conversor.produto(produto);
        }

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Produto p : produtoService.listarCatalogo(request.getParameter("busca"),
                Validador.converterInteiro(request.getParameter("categoria")),
                ProdutoService.normalizarOrdem(request.getParameter("ordem")))) {
            lista.add(conversor.produto(p));
        }
        return lista;
    }
}
