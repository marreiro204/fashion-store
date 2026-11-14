package br.com.fashionstore.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.service.CategoriaService;
import br.com.fashionstore.service.ProdutoService;
import br.com.fashionstore.util.ImagemUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Página inicial (index.jsp).
 *
 * O mapeamento "" (vazio) significa "a raiz do site":
 * http://localhost:8080/FashionStore/
 *
 * Monta a vitrine com dados reais:
 * - banners: imagens da pasta assets/images/banners/;
 * - categorias do banco, com a imagem de assets/images/categorias/<slug>.jpg;
 * - novidades: os últimos produtos cadastrados.
 *
 * Não está na lista de Servlets do PDF; é necessário porque a página
 * inicial passou a mostrar dados do banco.
 */
@WebServlet("")
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int QUANTIDADE_NOVIDADES = 8;

    private final CategoriaService categoriaService = new CategoriaService();
    private final ProdutoService produtoService = new ProdutoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("banners", ImagemUtil.banners(getServletContext()));

        try {
            List<Categoria> categorias = categoriaService.listar();
            Map<String, String> imagensCategorias = new LinkedHashMap<>();
            for (Categoria c : categorias) {
                String imagem = ImagemUtil.imagemCategoria(getServletContext(), c);
                if (imagem != null) {
                    imagensCategorias.put(c.getSlug(), imagem);
                }
            }
            request.setAttribute("categorias", categorias);
            request.setAttribute("imagensCategorias", imagensCategorias);
            request.setAttribute("novidades", produtoService.listarNovidades(QUANTIDADE_NOVIDADES));
        } catch (DAOException e) {
            log("Erro de banco na página inicial", e);
            request.setAttribute("erroGeral", "Não foi possível carregar os produtos agora.");
        }
        request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
    }
}
