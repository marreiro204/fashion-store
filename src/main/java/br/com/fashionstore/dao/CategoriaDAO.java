package br.com.fashionstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.util.ConnectionFactory;

/**
 * Acesso à tabela categoria.
 *
 * Padrão usado em todos os DAOs:
 * - SQL em constantes, sempre com "?" (PreparedStatement), NUNCA
 *   concatenando valores digitados pelo usuário -> evita SQL Injection;
 * - try-with-resources: Connection, PreparedStatement e ResultSet são
 *   fechados automaticamente, mesmo se der erro;
 * - SQLException convertida em DAOException.
 */
public class CategoriaDAO {

    private static final String INSERIR =
            "INSERT INTO categoria (nome, descricao) VALUES (?, ?)";
    private static final String ATUALIZAR =
            "UPDATE categoria SET nome = ?, descricao = ? WHERE id_categoria = ?";
    private static final String EXCLUIR =
            "DELETE FROM categoria WHERE id_categoria = ?";
    private static final String BUSCAR_POR_ID =
            "SELECT id_categoria, nome, descricao FROM categoria WHERE id_categoria = ?";
    private static final String LISTAR_TODAS =
            "SELECT id_categoria, nome, descricao FROM categoria ORDER BY nome";

    /** Insere e preenche o id gerado pelo banco no próprio objeto. */
    public Categoria inserir(Categoria categoria) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    categoria.setIdCategoria(chaves.getInt(1));
                }
            }
            return categoria;

        } catch (SQLException e) {
            throw DAOException.de("inserir categoria", e);
        }
    }

    /** @return true se a categoria existia e foi alterada */
    public boolean atualizar(Categoria categoria) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(ATUALIZAR)) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setInt(3, categoria.getIdCategoria());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("atualizar categoria", e);
        }
    }

    /**
     * @return true se a categoria foi excluída
     * @throws DAOException com motivo EM_USO se ainda houver produtos nela
     */
    public boolean excluir(int idCategoria) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(EXCLUIR)) {

            ps.setInt(1, idCategoria);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("excluir categoria", e);
        }
    }

    /** @return a categoria, ou null se não existir */
    public Categoria buscarPorId(int idCategoria) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(BUSCAR_POR_ID)) {

            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw DAOException.de("buscar categoria", e);
        }
    }

    public List<Categoria> listarTodas() {
        List<Categoria> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(LISTAR_TODAS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;

        } catch (SQLException e) {
            throw DAOException.de("listar categorias", e);
        }
    }

    /** Converte a linha atual do ResultSet em um objeto Categoria. */
    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getInt("id_categoria"),
                rs.getString("nome"),
                rs.getString("descricao"));
    }
}
