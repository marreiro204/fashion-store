package br.com.fashionstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.model.Perfil;
import br.com.fashionstore.util.ConnectionFactory;

/**
 * Acesso à tabela cliente.
 *
 * IMPORTANTE sobre a senha: este DAO grava exatamente o que estiver em
 * cliente.getSenha(). Quem chama (a camada Service, na Etapa 4) deve
 * colocar ali o HASH BCrypt, nunca a senha digitada.
 */
public class ClienteDAO {

    private final EnderecoDAO enderecoDAO = new EnderecoDAO();

    private static final String COLUNAS =
            "id_cliente, nome, email, senha, cpf, telefone, perfil";

    private static final String INSERIR =
            "INSERT INTO cliente (nome, email, senha, cpf, telefone, perfil) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String ATUALIZAR =
            "UPDATE cliente SET nome = ?, email = ?, cpf = ?, telefone = ? WHERE id_cliente = ?";
    private static final String ATUALIZAR_SENHA =
            "UPDATE cliente SET senha = ? WHERE id_cliente = ?";
    private static final String EXCLUIR =
            "DELETE FROM cliente WHERE id_cliente = ?";
    private static final String BUSCAR_POR_ID =
            "SELECT " + COLUNAS + " FROM cliente WHERE id_cliente = ?";
    private static final String BUSCAR_POR_EMAIL =
            "SELECT " + COLUNAS + " FROM cliente WHERE email = ?";
    private static final String LISTAR_TODOS =
            "SELECT " + COLUNAS + " FROM cliente ORDER BY nome";
    private static final String EXISTE_EMAIL =
            "SELECT 1 FROM cliente WHERE email = ?";
    private static final String EXISTE_CPF =
            "SELECT 1 FROM cliente WHERE cpf = ?";

    /**
     * Cadastra o cliente e, se houver, os endereços da lista dele.
     * Tudo em uma transação: se o endereço falhar, o cliente não é criado.
     *
     * @throws DAOException com motivo DUPLICADO se e-mail ou CPF já existirem
     */
    public Cliente inserir(Cliente cliente) {
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, cliente.getNome());
                    ps.setString(2, cliente.getEmail());
                    ps.setString(3, cliente.getSenha());
                    ps.setString(4, cliente.getCpf());
                    ps.setString(5, cliente.getTelefone());
                    ps.setString(6, cliente.getPerfil().name()); // enum -> "CLIENTE"/"ADMIN"
                    ps.executeUpdate();

                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                        if (chaves.next()) {
                            cliente.setIdCliente(chaves.getInt(1));
                        }
                    }
                }

                for (Endereco endereco : cliente.getEnderecos()) {
                    endereco.setIdCliente(cliente.getIdCliente());
                    enderecoDAO.inserir(con, endereco);
                }

                con.commit();
                return cliente;

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw DAOException.de("cadastrar cliente", e);
        }
    }

    /** Atualiza dados cadastrais. Senha e perfil NÃO são alterados aqui. */
    public boolean atualizar(Cliente cliente) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(ATUALIZAR)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getCpf());
            ps.setString(4, cliente.getTelefone());
            ps.setInt(5, cliente.getIdCliente());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("atualizar cliente", e);
        }
    }

    /** @param hashSenha senha JÁ convertida em hash BCrypt */
    public boolean atualizarSenha(int idCliente, String hashSenha) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(ATUALIZAR_SENHA)) {

            ps.setString(1, hashSenha);
            ps.setInt(2, idCliente);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("atualizar senha", e);
        }
    }

    /**
     * Exclui o cliente (os endereços vão junto, por CASCADE).
     *
     * @throws DAOException com motivo EM_USO se o cliente tiver pedidos
     */
    public boolean excluir(int idCliente) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(EXCLUIR)) {

            ps.setInt(1, idCliente);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw DAOException.de("excluir cliente", e);
        }
    }

    /** @return o cliente com seus endereços, ou null se não existir */
    public Cliente buscarPorId(int idCliente) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(BUSCAR_POR_ID)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Cliente cliente = mapear(rs);
                cliente.setEnderecos(enderecoDAO.listarPorCliente(con, idCliente));
                return cliente;
            }

        } catch (SQLException e) {
            throw DAOException.de("buscar cliente", e);
        }
    }

    /**
     * Usado no login: traz o cliente com o hash da senha para comparação.
     *
     * @return o cliente, ou null se o e-mail não estiver cadastrado
     */
    public Cliente buscarPorEmail(String email) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(BUSCAR_POR_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw DAOException.de("buscar cliente por e-mail", e);
        }
    }

    /** Lista para a área administrativa (sem endereços, para ser mais leve). */
    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(LISTAR_TODOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente c = mapear(rs);
                c.setSenha(null); // a listagem nunca precisa do hash
                lista.add(c);
            }
            return lista;

        } catch (SQLException e) {
            throw DAOException.de("listar clientes", e);
        }
    }

    public boolean existeEmail(String email) {
        return existe(EXISTE_EMAIL, email, "verificar e-mail");
    }

    public boolean existeCpf(String cpf) {
        return existe(EXISTE_CPF, cpf, "verificar CPF");
    }

    // ========================= Auxiliares =========================

    private boolean existe(String sql, String valor, String operacao) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw DAOException.de(operacao, e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNome(rs.getString("nome"));
        c.setEmail(rs.getString("email"));
        c.setSenha(rs.getString("senha"));
        c.setCpf(rs.getString("cpf"));
        c.setTelefone(rs.getString("telefone"));
        c.setPerfil(Perfil.valueOf(rs.getString("perfil"))); // "ADMIN" -> Perfil.ADMIN
        return c;
    }
}
