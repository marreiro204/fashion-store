package br.com.fashionstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.util.ConnectionFactory;

/**
 * Acesso à tabela estoque (uma linha por produto + tamanho).
 *
 * Alguns métodos recebem uma Connection como parâmetro. Eles são usados
 * DENTRO de transações de outros DAOs (ex.: ao salvar um pedido, a baixa
 * no estoque precisa usar a mesma conexão do pedido, para que tudo seja
 * confirmado ou desfeito junto).
 */
public class EstoqueDAO {

    private static final String COLUNAS = "id_estoque, id_produto, tamanho, quantidade";

    private static final String INSERIR =
            "INSERT INTO estoque (id_produto, tamanho, quantidade) VALUES (?, ?, ?)";
    private static final String ATUALIZAR =
            "UPDATE estoque SET tamanho = ?, quantidade = ? WHERE id_estoque = ?";
    private static final String ATUALIZAR_QUANTIDADE =
            "UPDATE estoque SET quantidade = ? WHERE id_produto = ? AND tamanho = ?";
    private static final String EXCLUIR =
            "DELETE FROM estoque WHERE id_estoque = ?";
    private static final String BUSCAR_POR_ID =
            "SELECT " + COLUNAS + " FROM estoque WHERE id_estoque = ?";
    private static final String BUSCAR_POR_PRODUTO_TAMANHO =
            "SELECT " + COLUNAS + " FROM estoque WHERE id_produto = ? AND tamanho = ?";
    private static final String LISTAR_POR_PRODUTO =
            "SELECT " + COLUNAS + " FROM estoque WHERE id_produto = ? ORDER BY id_estoque";

    /**
     * Baixa "segura": só subtrai se houver quantidade suficiente.
     * A verificação e a subtração acontecem no MESMO comando, então dois
     * clientes comprando a última peça ao mesmo tempo não deixam o
     * estoque negativo.
     */
    private static final String BAIXAR =
            "UPDATE estoque SET quantidade = quantidade - ? "
            + "WHERE id_produto = ? AND tamanho = ? AND quantidade >= ?";
    private static final String REPOR =
            "UPDATE estoque SET quantidade = quantidade + ? WHERE id_produto = ? AND tamanho = ?";

    // ===================== Operações com conexão própria =====================

    public Estoque inserir(Estoque estoque) {
        try (Connection con = ConnectionFactory.getConnection()) {
            return inserir(con, estoque);
        } catch (SQLException e) {
            throw DAOException.de("inserir estoque", e);
        }
    }

    /** Altera tamanho e quantidade de uma linha de estoque pelo id. */
    public boolean atualizar(Estoque estoque) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(ATUALIZAR)) {

            ps.setString(1, estoque.getTamanho());
            ps.setInt(2, estoque.getQuantidade());
            ps.setInt(3, estoque.getIdEstoque());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("atualizar estoque", e);
        }
    }

    /**
     * Define a quantidade de um tamanho. Se o tamanho ainda não existir
     * para o produto, cria a linha. Usado na tela de controle de estoque.
     */
    public void definirQuantidade(int idProduto, String tamanho, int quantidade) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(ATUALIZAR_QUANTIDADE)) {

            ps.setInt(1, quantidade);
            ps.setInt(2, idProduto);
            ps.setString(3, tamanho);

            if (ps.executeUpdate() == 0) {
                // Tamanho ainda não cadastrado para este produto: cria.
                inserir(con, new Estoque(idProduto, tamanho, quantidade));
            }

        } catch (SQLException e) {
            throw DAOException.de("definir quantidade em estoque", e);
        }
    }

    public boolean excluir(int idEstoque) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(EXCLUIR)) {

            ps.setInt(1, idEstoque);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("excluir estoque", e);
        }
    }

    /** @return o estoque, ou null se não existir */
    public Estoque buscarPorId(int idEstoque) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(BUSCAR_POR_ID)) {

            ps.setInt(1, idEstoque);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw DAOException.de("buscar estoque", e);
        }
    }

    /** @return o estoque do tamanho, ou null se o produto não tiver esse tamanho */
    public Estoque buscarPorProdutoETamanho(int idProduto, String tamanho) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(BUSCAR_POR_PRODUTO_TAMANHO)) {

            ps.setInt(1, idProduto);
            ps.setString(2, tamanho);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw DAOException.de("buscar estoque do tamanho", e);
        }
    }

    public List<Estoque> listarPorProduto(int idProduto) {
        try (Connection con = ConnectionFactory.getConnection()) {
            return listarPorProduto(con, idProduto);
        } catch (SQLException e) {
            throw DAOException.de("listar estoque do produto", e);
        }
    }

    // ============ Operações que usam a conexão de quem chamou (transações) ============

    Estoque inserir(Connection con, Estoque estoque) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, estoque.getIdProduto());
            ps.setString(2, estoque.getTamanho());
            ps.setInt(3, estoque.getQuantidade());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    estoque.setIdEstoque(chaves.getInt(1));
                }
            }
            return estoque;
        }
    }

    List<Estoque> listarPorProduto(Connection con, int idProduto) throws SQLException {
        List<Estoque> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(LISTAR_POR_PRODUTO)) {
            ps.setInt(1, idProduto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * @throws DAOException com motivo ESTOQUE_INSUFICIENTE se não houver
     *                      a quantidade pedida (nada é alterado)
     */
    void baixar(Connection con, int idProduto, String tamanho, int quantidade) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(BAIXAR)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, idProduto);
            ps.setString(3, tamanho);
            ps.setInt(4, quantidade);

            if (ps.executeUpdate() == 0) {
                throw new DAOException(DAOException.Motivo.ESTOQUE_INSUFICIENTE,
                        "Estoque insuficiente para o produto " + idProduto + ", tamanho " + tamanho);
            }
        }
    }

    void repor(Connection con, int idProduto, String tamanho, int quantidade) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(REPOR)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, idProduto);
            ps.setString(3, tamanho);
            ps.executeUpdate();
        }
    }

    private Estoque mapear(ResultSet rs) throws SQLException {
        Estoque e = new Estoque();
        e.setIdEstoque(rs.getInt("id_estoque"));
        e.setIdProduto(rs.getInt("id_produto"));
        e.setTamanho(rs.getString("tamanho"));
        e.setQuantidade(rs.getInt("quantidade"));
        return e;
    }
}
