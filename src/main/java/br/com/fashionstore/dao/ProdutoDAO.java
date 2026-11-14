package br.com.fashionstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.util.ConnectionFactory;

/**
 * Acesso à tabela produto.
 *
 * Os produtos são sempre carregados com a categoria (JOIN) e com a lista
 * de estoques por tamanho, porque as telas precisam dessas informações
 * (nome da categoria, tamanhos disponíveis, "esgotado").
 */
public class ProdutoDAO {

    private final EstoqueDAO estoqueDAO = new EstoqueDAO();

    private static final String SELECT_BASE =
            "SELECT p.id_produto, p.nome, p.descricao, p.preco, p.imagem, "
            + "c.id_categoria, c.nome AS categoria_nome, c.descricao AS categoria_descricao "
            + "FROM produto p INNER JOIN categoria c ON c.id_categoria = p.id_categoria ";

    private static final String INSERIR =
            "INSERT INTO produto (id_categoria, nome, descricao, preco, imagem) VALUES (?, ?, ?, ?, ?)";
    private static final String ATUALIZAR =
            "UPDATE produto SET id_categoria = ?, nome = ?, descricao = ?, preco = ?, imagem = ? "
            + "WHERE id_produto = ?";
    private static final String EXCLUIR =
            "DELETE FROM produto WHERE id_produto = ?";
    private static final String BUSCAR_POR_ID =
            SELECT_BASE + "WHERE p.id_produto = ?";
    private static final String LISTAR_TODOS =
            SELECT_BASE + "ORDER BY p.nome";

    /**
     * Insere o produto e, se ele tiver estoques na lista, insere também os
     * tamanhos. Tudo em UMA transação: ou grava tudo, ou nada.
     */
    public Produto inserir(Produto produto) {
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false); // início da transação
            try {
                try (PreparedStatement ps = con.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
                    preencher(ps, produto);
                    ps.executeUpdate();
                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                        if (chaves.next()) {
                            produto.setIdProduto(chaves.getInt(1));
                        }
                    }
                }

                for (Estoque estoque : produto.getEstoques()) {
                    estoque.setIdProduto(produto.getIdProduto());
                    estoqueDAO.inserir(con, estoque);
                }

                con.commit();   // confirma tudo
                return produto;

            } catch (SQLException | RuntimeException e) {
                con.rollback(); // desfaz tudo
                throw e;
            }
        } catch (SQLException e) {
            throw DAOException.de("inserir produto", e);
        }
    }

    /**
     * Atualiza os dados do produto (não mexe no estoque; para isso use o
     * EstoqueDAO).
     */
    public boolean atualizar(Produto produto) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(ATUALIZAR)) {

            preencher(ps, produto);
            ps.setInt(6, produto.getIdProduto());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("atualizar produto", e);
        }
    }

    /**
     * Exclui o produto (o estoque dele é apagado junto, por CASCADE).
     *
     * @throws DAOException com motivo EM_USO se o produto já foi vendido
     */
    public boolean excluir(int idProduto) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(EXCLUIR)) {

            ps.setInt(1, idProduto);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("excluir produto", e);
        }
    }

    /** @return o produto com categoria e estoques, ou null se não existir */
    public Produto buscarPorId(int idProduto) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(BUSCAR_POR_ID)) {

            ps.setInt(1, idProduto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Produto produto = mapear(rs);
                produto.setEstoques(estoqueDAO.listarPorProduto(con, idProduto));
                return produto;
            }

        } catch (SQLException e) {
            throw DAOException.de("buscar produto", e);
        }
    }

    public List<Produto> listarTodos() {
        return buscar(null, null);
    }

    /**
     * Pesquisa do catálogo: por texto (no nome ou na descrição) e/ou por
     * categoria. Parâmetros nulos ou vazios são ignorados.
     *
     * Ex.: buscar("jeans", null)  -> todos os produtos com "jeans"
     *      buscar(null, 2)        -> todos da categoria 2
     *      buscar("azul", 2)      -> os dois filtros juntos
     *
     * O SQL é montado aos poucos, mas os VALORES continuam indo pelos "?",
     * então continua protegido contra SQL Injection.
     */
    public List<Produto> buscar(String texto, Integer idCategoria) {
        return buscar(texto, idCategoria, ORDEM_NOME);
    }

    // Ordenações aceitas no catálogo. O texto do usuário NUNCA vai direto para o
    // SQL: ele só escolhe uma destas opções fixas (ORDER BY não aceita "?").
    public static final String ORDEM_NOME = "nome";
    public static final String ORDEM_MENOR_PRECO = "menor-preco";
    public static final String ORDEM_MAIOR_PRECO = "maior-preco";
    public static final String ORDEM_NOVIDADES = "novidades";

    private static String clausulaOrdem(String ordem) {
        if (ordem == null) {
            return "ORDER BY p.nome";
        }
        return switch (ordem) {
            case ORDEM_MENOR_PRECO -> "ORDER BY p.preco ASC, p.nome";
            case ORDEM_MAIOR_PRECO -> "ORDER BY p.preco DESC, p.nome";
            case ORDEM_NOVIDADES -> "ORDER BY p.id_produto DESC";   // cadastrados por último primeiro
            default -> "ORDER BY p.nome";
        };
    }

    /** Igual à pesquisa acima, escolhendo a ordenação (ORDEM_...). */
    public List<Produto> buscar(String texto, Integer idCategoria, String ordem) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sql.append("AND (p.nome LIKE ? OR p.descricao LIKE ?) ");
            String padrao = "%" + escaparLike(texto.trim()) + "%";
            parametros.add(padrao);
            parametros.add(padrao);
        }
        if (idCategoria != null) {
            sql.append("AND p.id_categoria = ? ");
            parametros.add(idCategoria);
        }
        sql.append(clausulaOrdem(ordem));

        List<Produto> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            // Carrega os tamanhos de cada produto (mesma conexão).
            for (Produto p : lista) {
                p.setEstoques(estoqueDAO.listarPorProduto(con, p.getIdProduto()));
            }
            return lista;

        } catch (SQLException e) {
            throw DAOException.de("pesquisar produtos", e);
        }
    }

    // ========================= Auxiliares =========================

    /** Preenche os 5 primeiros "?" de INSERIR e ATUALIZAR (mesma ordem). */
    private void preencher(PreparedStatement ps, Produto produto) throws SQLException {
        ps.setInt(1, produto.getCategoria().getIdCategoria());
        ps.setString(2, produto.getNome());
        ps.setString(3, produto.getDescricao());
        ps.setBigDecimal(4, produto.getPreco());
        ps.setString(5, produto.getImagem());
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getInt("id_categoria"),
                rs.getString("categoria_nome"),
                rs.getString("categoria_descricao"));

        Produto p = new Produto();
        p.setIdProduto(rs.getInt("id_produto"));
        p.setCategoria(categoria);
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setImagem(rs.getString("imagem"));
        return p;
    }

    /**
     * No LIKE, "%" e "_" são curingas. Se o usuário digitar "100%",
     * escapamos para procurar o caractere literalmente.
     */
    private String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
