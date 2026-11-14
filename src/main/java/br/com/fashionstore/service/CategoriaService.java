package br.com.fashionstore.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.dao.CategoriaDAO;
import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.util.Validador;

/**
 * Regras de negócio das categorias.
 */
public class CategoriaService {

    private final CategoriaDAO categoriaDAO;

    public CategoriaService() {
        this(new CategoriaDAO());
    }

    public CategoriaService(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public List<Categoria> listar() {
        return categoriaDAO.listarTodas();
    }

    /** @return a categoria, ou null se não existir */
    public Categoria buscar(int idCategoria) {
        return categoriaDAO.buscarPorId(idCategoria);
    }

    /**
     * Cadastra (id = 0) ou altera (id > 0) uma categoria.
     *
     * @throws ValidacaoException nome vazio, muito longo ou já existente
     */
    public Categoria salvar(Categoria categoria) throws ValidacaoException {
        categoria.setNome(Validador.limpar(categoria.getNome()));
        String descricao = Validador.limpar(categoria.getDescricao());
        categoria.setDescricao(descricao.isEmpty() ? null : descricao);

        Map<String, String> erros = new LinkedHashMap<>();
        if (categoria.getNome().length() < 2) {
            erros.put("nome", "Informe o nome da categoria (mínimo 2 caracteres).");
        } else if (Validador.excede(categoria.getNome(), 50)) {
            erros.put("nome", "O nome pode ter no máximo 50 caracteres.");
        }
        if (Validador.excede(categoria.getDescricao(), 255)) {
            erros.put("descricao", "A descrição pode ter no máximo 255 caracteres.");
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        try {
            if (categoria.getIdCategoria() == 0) {
                categoriaDAO.inserir(categoria);
            } else if (!categoriaDAO.atualizar(categoria)) {
                throw new ValidacaoException("geral", "Categoria não encontrada.");
            }
            MenuCategorias.limpar(); // o menu do site mostra a mudança na hora
            return categoria;
        } catch (DAOException e) {
            if (e.getMotivo() == DAOException.Motivo.DUPLICADO) {
                throw new ValidacaoException("nome", "Já existe uma categoria com esse nome.");
            }
            throw e;
        }
    }

    /**
     * @throws ValidacaoException se a categoria não existir ou ainda tiver produtos
     */
    public void excluir(int idCategoria) throws ValidacaoException {
        try {
            if (!categoriaDAO.excluir(idCategoria)) {
                throw new ValidacaoException("geral", "Categoria não encontrada.");
            }
            MenuCategorias.limpar();
        } catch (DAOException e) {
            if (e.getMotivo() == DAOException.Motivo.EM_USO) {
                throw new ValidacaoException("geral",
                        "Não é possível excluir: existem produtos nesta categoria. "
                        + "Mova ou exclua os produtos antes.");
            }
            throw e;
        }
    }
}
