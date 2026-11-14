package br.com.fashionstore.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.dao.CategoriaDAO;
import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.util.Validador;

/**
 * Regras de negócio dos produtos: catálogo, pesquisa e CRUD do admin.
 *
 * O estoque por tamanho é tratado na Etapa 6 (EstoqueService).
 */
public class ProdutoService {

    /** Limite do DECIMAL(10,2) do banco: 8 dígitos antes da vírgula. */
    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("99999999.99");

    private final ProdutoDAO produtoDAO;
    private final CategoriaDAO categoriaDAO;

    public ProdutoService() {
        this(new ProdutoDAO(), new CategoriaDAO());
    }

    public ProdutoService(ProdutoDAO produtoDAO, CategoriaDAO categoriaDAO) {
        this.produtoDAO = produtoDAO;
        this.categoriaDAO = categoriaDAO;
    }

    /**
     * Catálogo com pesquisa e filtro. Parâmetros vazios/nulos = sem filtro.
     * Texto de pesquisa é limitado a 100 caracteres.
     */
    public List<Produto> listarCatalogo(String busca, Integer idCategoria) {
        return listarCatalogo(busca, idCategoria, null);
    }

    /**
     * Catálogo com ordenação: "nome" (padrão), "menor-preco", "maior-preco"
     * ou "novidades". Qualquer outro valor vira "nome".
     */
    public List<Produto> listarCatalogo(String busca, Integer idCategoria, String ordem) {
        String texto = Validador.limpar(busca);
        if (texto.length() > 100) {
            texto = texto.substring(0, 100);
        }
        return produtoDAO.buscar(texto, idCategoria, normalizarOrdem(ordem));
    }

    /** Os produtos cadastrados mais recentemente (seção "Novidades" da página inicial). */
    public List<Produto> listarNovidades(int quantidade) {
        List<Produto> todos = produtoDAO.buscar(null, null, ProdutoDAO.ORDEM_NOVIDADES);
        return todos.size() > quantidade ? todos.subList(0, quantidade) : todos;
    }

    /** Garante que só uma das ordenações conhecidas seja usada. */
    public static String normalizarOrdem(String ordem) {
        if (ProdutoDAO.ORDEM_MENOR_PRECO.equals(ordem) || ProdutoDAO.ORDEM_MAIOR_PRECO.equals(ordem)
                || ProdutoDAO.ORDEM_NOVIDADES.equals(ordem)) {
            return ordem;
        }
        return ProdutoDAO.ORDEM_NOME;
    }

    /** @return o produto com categoria e estoques, ou null se não existir */
    public Produto buscar(int idProduto) {
        return produtoDAO.buscarPorId(idProduto);
    }

    /**
     * Cadastra (id = 0) ou altera (id > 0) um produto.
     *
     * @param produto     nome, descrição e imagem digitados (e o id, na alteração)
     * @param precoTexto  preço como digitado ("59,90", "59.90"...)
     * @param idCategoria categoria escolhida
     * @throws ValidacaoException com os erros por campo
     */
    public Produto salvar(Produto produto, String precoTexto, Integer idCategoria) throws ValidacaoException {
        produto.setNome(Validador.limpar(produto.getNome()));
        String descricao = Validador.limpar(produto.getDescricao());
        produto.setDescricao(descricao.isEmpty() ? null : descricao);
        String imagem = Validador.limpar(produto.getImagem());
        produto.setImagem(imagem.isEmpty() ? null : imagem);

        Map<String, String> erros = new LinkedHashMap<>();

        if (produto.getNome().length() < 3) {
            erros.put("nome", "Informe o nome do produto (mínimo 3 caracteres).");
        } else if (Validador.excede(produto.getNome(), 100)) {
            erros.put("nome", "O nome pode ter no máximo 100 caracteres.");
        }

        if (Validador.excede(produto.getDescricao(), 1000)) {
            erros.put("descricao", "A descrição pode ter no máximo 1000 caracteres.");
        }

        BigDecimal preco = Validador.converterPreco(precoTexto);
        if (preco == null) {
            erros.put("preco", "Informe um preço válido. Ex.: 59,90");
        } else if (preco.signum() <= 0) {
            erros.put("preco", "O preço deve ser maior que zero.");
        } else if (preco.compareTo(PRECO_MAXIMO) > 0) {
            erros.put("preco", "Preço acima do limite permitido.");
        }
        produto.setPreco(preco);

        if (produto.getImagem() != null
                && (!Validador.imagemValida(produto.getImagem()) || Validador.excede(produto.getImagem(), 255))) {
            erros.put("imagem", "Use um nome de arquivo (ex.: camiseta-azul.jpg) ou um endereço http(s)://");
        }

        Categoria categoria = idCategoria == null ? null : categoriaDAO.buscarPorId(idCategoria);
        if (categoria == null) {
            erros.put("categoria", "Selecione uma categoria.");
        }
        produto.setCategoria(categoria);

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        if (produto.getIdProduto() == 0) {
            produto.getEstoques().clear(); // os tamanhos são cadastrados na tela de estoque
            produtoDAO.inserir(produto);
        } else if (!produtoDAO.atualizar(produto)) {
            throw new ValidacaoException("geral", "Produto não encontrado.");
        }
        return produto;
    }

    /**
     * @throws ValidacaoException se o produto não existir ou já tiver sido vendido
     */
    public void excluir(int idProduto) throws ValidacaoException {
        try {
            if (!produtoDAO.excluir(idProduto)) {
                throw new ValidacaoException("geral", "Produto não encontrado.");
            }
        } catch (DAOException e) {
            if (e.getMotivo() == DAOException.Motivo.EM_USO) {
                throw new ValidacaoException("geral",
                        "Este produto já foi vendido e não pode ser excluído, para não apagar o histórico "
                        + "de pedidos. Para tirá-lo de venda, zere o estoque.");
            }
            throw e;
        }
    }
}
