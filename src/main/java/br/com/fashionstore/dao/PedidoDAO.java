package br.com.fashionstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.ItemPedido;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.StatusPedido;
import br.com.fashionstore.util.ConnectionFactory;

/**
 * Acesso à tabela pedido.
 *
 * Aqui estão as duas TRANSAÇÕES mais importantes do sistema:
 *
 * 1. salvar(pedido): grava o pedido, grava cada item e dá baixa no
 *    estoque de cada tamanho. Se QUALQUER passo falhar (ex.: estoque
 *    acabou), tudo é desfeito (rollback): não fica pedido sem item,
 *    nem estoque baixado sem pedido.
 *
 * 2. atualizarStatus(id, CANCELADO): muda o status e devolve as peças
 *    ao estoque, também tudo junto.
 */
public class PedidoDAO {

    private final ItemPedidoDAO itemPedidoDAO = new ItemPedidoDAO();
    private final EstoqueDAO estoqueDAO = new EstoqueDAO();

    private static final String INSERIR =
            "INSERT INTO pedido (id_cliente, id_endereco, data_pedido, valor_total, status) "
            + "VALUES (?, ?, ?, ?, ?)";

    /** Pedido + cliente (sem senha) + endereço, em uma consulta só. */
    private static final String SELECT_BASE =
            "SELECT pe.id_pedido, pe.data_pedido, pe.valor_total, pe.status, "
            + "c.id_cliente, c.nome AS cliente_nome, c.email AS cliente_email, "
            + "e.id_endereco, e.cep, e.logradouro, e.numero, e.complemento, "
            + "e.bairro, e.cidade, e.estado "
            + "FROM pedido pe "
            + "INNER JOIN cliente c ON c.id_cliente = pe.id_cliente "
            + "INNER JOIN endereco e ON e.id_endereco = pe.id_endereco ";

    private static final String BUSCAR_POR_ID =
            SELECT_BASE + "WHERE pe.id_pedido = ?";
    private static final String LISTAR_POR_CLIENTE =
            SELECT_BASE + "WHERE pe.id_cliente = ? ORDER BY pe.data_pedido DESC, pe.id_pedido DESC";
    private static final String LISTAR_TODOS =
            SELECT_BASE + "ORDER BY pe.data_pedido DESC, pe.id_pedido DESC";
    private static final String LISTAR_POR_STATUS =
            SELECT_BASE + "WHERE pe.status = ? ORDER BY pe.data_pedido DESC, pe.id_pedido DESC";

    /** FOR UPDATE: trava a linha até o fim da transação (evita dois cancelamentos ao mesmo tempo). */
    private static final String STATUS_ATUAL =
            "SELECT status FROM pedido WHERE id_pedido = ? FOR UPDATE";
    private static final String ATUALIZAR_STATUS =
            "UPDATE pedido SET status = ? WHERE id_pedido = ?";
    private static final String EXCLUIR =
            "DELETE FROM pedido WHERE id_pedido = ?";

    /**
     * Grava o pedido com todos os itens e dá baixa no estoque (TRANSAÇÃO).
     *
     * @throws DAOException com motivo ESTOQUE_INSUFICIENTE se algum tamanho
     *                      não tiver a quantidade pedida (nada é gravado)
     */
    public Pedido salvar(Pedido pedido) {
        if (pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido precisa ter pelo menos um item.");
        }

        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false); // ---- início da transação ----
            try {
                // 1. Pedido
                try (PreparedStatement ps = con.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, pedido.getCliente().getIdCliente());
                    ps.setInt(2, pedido.getEndereco().getIdEndereco());
                    // DATETIME guarda até os segundos: removemos a fração para não arredondar.
                    pedido.setDataPedido(pedido.getDataPedido().withNano(0));
                    ps.setObject(3, pedido.getDataPedido());
                    ps.setBigDecimal(4, pedido.calcularValorTotal());
                    ps.setString(5, pedido.getStatus().name());
                    ps.executeUpdate();

                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                        if (chaves.next()) {
                            pedido.setIdPedido(chaves.getInt(1));
                        }
                    }
                }

                // 2. Itens + baixa no estoque
                for (ItemPedido item : pedido.getItens()) {
                    estoqueDAO.baixar(con, item.getProduto().getIdProduto(),
                            item.getTamanho(), item.getQuantidade());
                    item.setIdPedido(pedido.getIdPedido());
                    itemPedidoDAO.inserir(con, item);
                }

                con.commit(); // ---- tudo certo: confirma ----
                return pedido;

            } catch (SQLException | RuntimeException e) {
                con.rollback(); // ---- algo falhou: desfaz tudo ----
                pedido.setIdPedido(0);
                throw e;
            }
        } catch (SQLException e) {
            throw DAOException.de("salvar pedido", e);
        }
    }

    /**
     * Atualiza o status. Se o novo status for CANCELADO, devolve as peças
     * ao estoque na mesma transação.
     *
     * A regra de QUAIS mudanças são permitidas (ex.: entregue não volta a
     * pendente) fica na camada Service; aqui só garantimos que o estoque
     * não seja devolvido duas vezes.
     *
     * @return false se o pedido não existir
     */
    public boolean atualizarStatus(int idPedido, StatusPedido novoStatus) {
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);
            try {
                StatusPedido atual;
                try (PreparedStatement ps = con.prepareStatement(STATUS_ATUAL)) {
                    ps.setInt(1, idPedido);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        atual = StatusPedido.valueOf(rs.getString("status"));
                    }
                }

                if (novoStatus == StatusPedido.CANCELADO && atual != StatusPedido.CANCELADO) {
                    for (ItemPedido item : itemPedidoDAO.listarPorPedido(con, idPedido)) {
                        estoqueDAO.repor(con, item.getProduto().getIdProduto(),
                                item.getTamanho(), item.getQuantidade());
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(ATUALIZAR_STATUS)) {
                    ps.setString(1, novoStatus.name());
                    ps.setInt(2, idPedido);
                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw DAOException.de("atualizar status do pedido", e);
        }
    }

    /**
     * Exclui o pedido e seus itens (CASCADE). NÃO devolve estoque.
     * Uso administrativo/testes: no dia a dia, prefira cancelar
     * (atualizarStatus com CANCELADO), que mantém o histórico.
     */
    public boolean excluir(int idPedido) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(EXCLUIR)) {

            ps.setInt(1, idPedido);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("excluir pedido", e);
        }
    }

    /** @return o pedido completo (com itens), ou null se não existir */
    public Pedido buscarPorId(int idPedido) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(BUSCAR_POR_ID)) {

            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Pedido pedido = mapear(rs);
                pedido.setItens(itemPedidoDAO.listarPorPedido(con, idPedido));
                return pedido;
            }

        } catch (SQLException e) {
            throw DAOException.de("buscar pedido", e);
        }
    }

    /**
     * Histórico do cliente, do mais recente para o mais antigo.
     * Os itens não são carregados (para os detalhes, use buscarPorId).
     */
    public List<Pedido> listarPorCliente(int idCliente) {
        return listar(LISTAR_POR_CLIENTE, idCliente, "listar pedidos do cliente");
    }

    /** Todos os pedidos (área administrativa), sem itens. */
    public List<Pedido> listarTodos() {
        return listar(LISTAR_TODOS, null, "listar pedidos");
    }

    /** Pedidos com um status (filtro da área administrativa), sem itens. */
    public List<Pedido> listarPorStatus(StatusPedido status) {
        return listar(LISTAR_POR_STATUS, status.name(), "listar pedidos por status");
    }

    // ========================= Auxiliares =========================

    private List<Pedido> listar(String sql, Object parametro, String operacao) {
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (parametro != null) {
                ps.setObject(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            throw DAOException.de(operacao, e);
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNome(rs.getString("cliente_nome"));
        cliente.setEmail(rs.getString("cliente_email"));

        Pedido pedido = new Pedido();
        pedido.setIdPedido(rs.getInt("id_pedido"));
        pedido.setCliente(cliente);
        pedido.setEndereco(EnderecoDAO.mapear(rs));
        pedido.setDataPedido(rs.getObject("data_pedido", LocalDateTime.class));
        pedido.setValorTotal(rs.getBigDecimal("valor_total"));
        pedido.setStatus(StatusPedido.valueOf(rs.getString("status")));
        return pedido;
    }
}
