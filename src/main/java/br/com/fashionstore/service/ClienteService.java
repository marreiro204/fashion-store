package br.com.fashionstore.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.dao.ClienteDAO;
import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.model.Perfil;
import br.com.fashionstore.util.SenhaUtil;
import br.com.fashionstore.util.Validador;

/**
 * Regras de negócio de clientes: cadastro e autenticação.
 *
 * Por que uma camada Service?
 * - O Servlet só cuida de HTTP (ler parâmetros, escolher a página);
 * - O DAO só cuida de SQL;
 * - As REGRAS (validar CPF, gerar hash, impedir e-mail repetido) ficam
 *   aqui, e poderão ser reaproveitadas pela futura API JSON do Flutter.
 */
public class ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteService() {
        this(new ClienteDAO());
    }

    /** Permite trocar o DAO (usado nos testes, com um DAO "falso" em memória). */
    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    /**
     * Cadastra um novo cliente com um endereço.
     *
     * @param cliente      dados digitados (nome, email, cpf, telefone)
     * @param senha        senha digitada (texto puro; aqui vira hash)
     * @param confirmacao  repetição da senha
     * @param endereco     endereço de entrega
     * @return o cliente cadastrado (com id, sem senha)
     * @throws ValidacaoException se algum dado estiver inválido ou já cadastrado
     */
    public Cliente cadastrar(Cliente cliente, String senha, String confirmacao, Endereco endereco)
            throws ValidacaoException {

        normalizar(cliente, endereco);

        Map<String, String> erros = new LinkedHashMap<>();
        validarCliente(cliente, erros);
        validarSenha(senha, confirmacao, erros);
        validarEndereco(endereco, erros);

        if (erros.isEmpty()) {
            // Só consulta o banco se o formato estiver correto.
            if (clienteDAO.existeEmail(cliente.getEmail())) {
                erros.put("email", "Este e-mail já está cadastrado.");
            }
            if (clienteDAO.existeCpf(cliente.getCpf())) {
                erros.put("cpf", "Este CPF já está cadastrado.");
            }
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        cliente.setSenha(SenhaUtil.gerarHash(senha));
        cliente.setPerfil(Perfil.CLIENTE); // cadastro público nunca cria administrador
        cliente.getEnderecos().clear();
        cliente.adicionarEndereco(endereco);

        try {
            clienteDAO.inserir(cliente);
        } catch (DAOException e) {
            // Dois cadastros iguais ao mesmo tempo: o UNIQUE do banco barra o segundo.
            if (e.getMotivo() == DAOException.Motivo.DUPLICADO) {
                throw new ValidacaoException("email", "E-mail ou CPF já cadastrado.");
            }
            throw e;
        }

        cliente.setSenha(null);
        return cliente;
    }

    /**
     * Confere e-mail e senha.
     *
     * @return o cliente (sem a senha) se estiver correto; null caso contrário.
     *         Não dizemos se o erro foi no e-mail ou na senha, para não
     *         revelar quais e-mails estão cadastrados.
     */
    public Cliente autenticar(String email, String senha) {
        if (Validador.vazio(email) || Validador.vazio(senha)) {
            return null;
        }
        Cliente cliente = clienteDAO.buscarPorEmail(email.trim().toLowerCase());
        String hash = cliente == null ? null : cliente.getSenha();

        if (!SenhaUtil.conferir(senha, hash)) {
            return null;
        }
        cliente.setSenha(null);
        return cliente;
    }

    /** Dados atualizados do cliente (com endereços), para a página de perfil. */
    public Cliente buscarPerfil(int idCliente) {
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente != null) {
            cliente.setSenha(null);
        }
        return cliente;
    }

    /**
     * Consulta de clientes (admin), com pesquisa opcional por nome, e-mail
     * ou CPF. Os administradores não aparecem na lista.
     */
    public List<Cliente> listarClientes(String busca) {
        String texto = Validador.limpar(busca).toLowerCase();
        String digitos = Validador.somenteDigitos(busca);

        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : clienteDAO.listarTodos()) {
            if (c.isAdmin()) {
                continue;
            }
            boolean combina = texto.isEmpty()
                    || c.getNome().toLowerCase().contains(texto)
                    || c.getEmail().toLowerCase().contains(texto)
                    || (!digitos.isEmpty() && c.getCpf().contains(digitos));
            if (combina) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    // ========================= Auxiliares =========================

    /** Padroniza o formato antes de validar e gravar. */
    private void normalizar(Cliente c, Endereco e) {
        c.setNome(Validador.limpar(c.getNome()));
        c.setEmail(Validador.limpar(c.getEmail()).toLowerCase());
        c.setCpf(Validador.somenteDigitos(c.getCpf()));
        String telefone = Validador.somenteDigitos(c.getTelefone());
        c.setTelefone(telefone.isEmpty() ? null : telefone);

        e.setCep(Validador.somenteDigitos(e.getCep()));
        e.setLogradouro(Validador.limpar(e.getLogradouro()));
        e.setNumero(Validador.limpar(e.getNumero()));
        String complemento = Validador.limpar(e.getComplemento());
        e.setComplemento(complemento.isEmpty() ? null : complemento);
        e.setBairro(Validador.limpar(e.getBairro()));
        e.setCidade(Validador.limpar(e.getCidade()));
        e.setEstado(Validador.limpar(e.getEstado()).toUpperCase());
    }

    private void validarCliente(Cliente c, Map<String, String> erros) {
        if (c.getNome().length() < 3) {
            erros.put("nome", "Informe seu nome completo.");
        } else if (Validador.excede(c.getNome(), 100)) {
            erros.put("nome", "O nome pode ter no máximo 100 caracteres.");
        }

        if (!Validador.emailValido(c.getEmail())) {
            erros.put("email", "Informe um e-mail válido.");
        } else if (Validador.excede(c.getEmail(), 150)) {
            erros.put("email", "O e-mail pode ter no máximo 150 caracteres.");
        }

        if (!Validador.cpfValido(c.getCpf())) {
            erros.put("cpf", "CPF inválido.");
        }

        if (c.getTelefone() != null && !Validador.telefoneValido(c.getTelefone())) {
            erros.put("telefone", "Telefone deve ter DDD + número (10 ou 11 dígitos).");
        }
    }

    private void validarSenha(String senha, String confirmacao, Map<String, String> erros) {
        if (!Validador.senhaForte(senha)) {
            erros.put("senha", "A senha deve ter de 8 a 72 caracteres, com letras e números.");
        } else if (!senha.equals(confirmacao)) {
            erros.put("confirmacao", "As senhas não conferem.");
        }
    }

    private void validarEndereco(Endereco e, Map<String, String> erros) {
        if (!Validador.cepValido(e.getCep())) {
            erros.put("cep", "CEP deve ter 8 números.");
        }
        obrigatorio(e.getLogradouro(), 150, "logradouro", "Informe a rua/avenida.", erros);
        obrigatorio(e.getNumero(), 10, "numero", "Informe o número.", erros);
        if (Validador.excede(e.getComplemento(), 100)) {
            erros.put("complemento", "Máximo de 100 caracteres.");
        }
        obrigatorio(e.getBairro(), 100, "bairro", "Informe o bairro.", erros);
        obrigatorio(e.getCidade(), 100, "cidade", "Informe a cidade.", erros);
        if (!Validador.ufValida(e.getEstado())) {
            erros.put("estado", "Selecione o estado.");
        }
    }

    private void obrigatorio(String valor, int maximo, String campo, String mensagem,
                             Map<String, String> erros) {
        if (Validador.vazio(valor)) {
            erros.put(campo, mensagem);
        } else if (Validador.excede(valor, maximo)) {
            erros.put(campo, "Máximo de " + maximo + " caracteres.");
        }
    }
}
