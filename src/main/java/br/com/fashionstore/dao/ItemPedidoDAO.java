package br.com.fashionstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.model.ItemPedido;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.util.ConnectionFactory;

/**
 * Acesso à tabela item_pedido.
 *
 * Decisão: itens NÃO têm "atualizar" nem "excluir" individuais.
 * Depois que o pedido é finalizado, os itens são o registro do que foi
 * comprado e pelo preço da época; alterá-los quebraria o histórico e o
 * valor_total. Itens são:
 * - inseridos junto com o pedido (PedidoDAO.salvar, em transação);
 * - apagados junto com o pedido (CASCADE no banco).
 */
public class ItemPedidoDAO {

    private static final String INSERIR =
            "INSERT INTO item_pedido (id_pedido, id_produto, tamanho, quantidade, preco_unitario, subtotal) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BASE =
            "SELECT i.id_item_pedido, i.id_pedido, i.tamanho, i.quantidade, i.preco_unitario, i.subtotal, "
            + "p.id_produto, p.nome AS produto_nome, p.imagem AS produto_imagem, p.preco AS produto_preco "
            + "FROM item_pedido i INNER JOIN produto p ON p.id_produto = i.id_produto ";

    private static final String BUSCAR_POR_ID =
            SELECT_BASE + "WHERE i.id_item_pedido = ?";
    private static final String LISTAR_POR_PEDIDO =
            SELECT_BASE + "WHERE i.id_pedido = ? ORDER BY i.id_item_pedido";

    /** @return o item com dados básicos do produto, ou null se não existir */
    public ItemPedido buscarPorId(int idItemPedido) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(BUSCAR_POR_ID)) {

            ps.setInt(1, idItemPedido);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw DAOException.de("buscar item do pedido", e);
        }
    }

    public List<ItemPedido> listarPorPedido(int idPedido) {
        try (Connection con = ConnectionFactory.getConnection()) {
            return listarPorPedido(con, idPedido);
        } catch (SQLException e) {
            throw DAOException.de("listar itens do pedido", e);
        }
    }

    // ============ Usados dentro de transações (PedidoDAO) ============

    ItemPedido inserir(Connection con, ItemPedido item) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getIdPedido());
            ps.setInt(2, item.getProduto().getIdProduto());
            ps.setString(3, item.getTamanho());
            ps.setInt(4, item.getQuantidade());
            ps.setBigDecimal(5, item.getPrecoUnitario());
            ps.setBigDecimal(6, item.getSubtotal());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    item.setIdItemPedido(chaves.getInt(1));
                }
            }
            return item;
        }
    }

    List<ItemPedido> listarPorPedido(Connection con, int idPedido) throws SQLException {
        List<ItemPedido> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(LISTAR_POR_PEDIDO)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private ItemPedido mapear(ResultSet rs) throws SQLException {
        Produto produto = new Produto();
        produto.setIdProduto(rs.getInt("id_produto"));
        produto.setNome(rs.getString("produto_nome"));
        produto.setImagem(rs.getString("produto_imagem"));
        produto.setPreco(rs.getBigDecimal("produto_preco")); // preço ATUAL do produto

        ItemPedido item = new ItemPedido();
        item.setIdItemPedido(rs.getInt("id_item_pedido"));
        item.setIdPedido(rs.getInt("id_pedido"));
        item.setProduto(produto);
        item.setTamanho(rs.getString("tamanho"));
        item.setQuantidade(rs.getInt("quantidade"));
        item.setPrecoUnitario(rs.getBigDecimal("preco_unitario")); // preço PAGO
        item.setSubtotal(rs.getBigDecimal("subtotal"));            // valor gravado
        return item;
    }
}
