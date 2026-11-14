package br.com.fashionstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.util.ConnectionFactory;

/**
 * Acesso à tabela endereco.
 */
public class EnderecoDAO {

    private static final String COLUNAS =
            "id_endereco, id_cliente, cep, logradouro, numero, complemento, bairro, cidade, estado";

    private static final String INSERIR =
            "INSERT INTO endereco (id_cliente, cep, logradouro, numero, complemento, bairro, cidade, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String ATUALIZAR =
            "UPDATE endereco SET cep = ?, logradouro = ?, numero = ?, complemento = ?, "
            + "bairro = ?, cidade = ?, estado = ? WHERE id_endereco = ?";
    private static final String EXCLUIR =
            "DELETE FROM endereco WHERE id_endereco = ?";
    private static final String BUSCAR_POR_ID =
            "SELECT " + COLUNAS + " FROM endereco WHERE id_endereco = ?";
    private static final String LISTAR_POR_CLIENTE =
            "SELECT " + COLUNAS + " FROM endereco WHERE id_cliente = ? ORDER BY id_endereco";

    public Endereco inserir(Endereco endereco) {
        try (Connection con = ConnectionFactory.getConnection()) {
            return inserir(con, endereco);
        } catch (SQLException e) {
            throw DAOException.de("inserir endereço", e);
        }
    }

    public boolean atualizar(Endereco endereco) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(ATUALIZAR)) {

            ps.setString(1, endereco.getCep());
            ps.setString(2, endereco.getLogradouro());
            ps.setString(3, endereco.getNumero());
            ps.setString(4, endereco.getComplemento());
            ps.setString(5, endereco.getBairro());
            ps.setString(6, endereco.getCidade());
            ps.setString(7, endereco.getEstado());
            ps.setInt(8, endereco.getIdEndereco());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("atualizar endereço", e);
        }
    }

    /**
     * @throws DAOException com motivo EM_USO se o endereço já foi usado em um pedido
     */
    public boolean excluir(int idEndereco) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(EXCLUIR)) {

            ps.setInt(1, idEndereco);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("excluir endereço", e);
        }
    }

    /** @return o endereço, ou null se não existir */
    public Endereco buscarPorId(int idEndereco) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(BUSCAR_POR_ID)) {

            ps.setInt(1, idEndereco);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw DAOException.de("buscar endereço", e);
        }
    }

    public List<Endereco> listarPorCliente(int idCliente) {
        try (Connection con = ConnectionFactory.getConnection()) {
            return listarPorCliente(con, idCliente);
        } catch (SQLException e) {
            throw DAOException.de("listar endereços do cliente", e);
        }
    }

    // ============ Usados dentro de transações (ClienteDAO) ============

    Endereco inserir(Connection con, Endereco endereco) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, endereco.getIdCliente());
            ps.setString(2, endereco.getCep());
            ps.setString(3, endereco.getLogradouro());
            ps.setString(4, endereco.getNumero());
            ps.setString(5, endereco.getComplemento());
            ps.setString(6, endereco.getBairro());
            ps.setString(7, endereco.getCidade());
            ps.setString(8, endereco.getEstado());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    endereco.setIdEndereco(chaves.getInt(1));
                }
            }
            return endereco;
        }
    }

    List<Endereco> listarPorCliente(Connection con, int idCliente) throws SQLException {
        List<Endereco> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(LISTAR_POR_CLIENTE)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Também usado pelo PedidoDAO (colunas com os mesmos nomes). */
    static Endereco mapear(ResultSet rs) throws SQLException {
        Endereco e = new Endereco();
        e.setIdEndereco(rs.getInt("id_endereco"));
        e.setIdCliente(rs.getInt("id_cliente"));
        e.setCep(rs.getString("cep"));
        e.setLogradouro(rs.getString("logradouro"));
        e.setNumero(rs.getString("numero"));
        e.setComplemento(rs.getString("complemento"));
        e.setBairro(rs.getString("bairro"));
        e.setCidade(rs.getString("cidade"));
        e.setEstado(rs.getString("estado"));
        return e;
    }
}
