package br.com.fashionstore.api;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.service.TokenService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Conta do cliente.
 *
 *   POST /api/clientes  -> cadastro (devolve 201 + token, já entra logado)
 *   POST /api/login     -> {email, senha} -> {token, cliente}
 *   POST /api/logout    -> invalida o token
 *   GET  /api/perfil    -> dados do cliente logado (com endereços)
 *
 * Usa o mesmo ClienteService do site: mesmas validações (CPF, senha forte,
 * e-mail único) e mesma senha com BCrypt.
 */
@WebServlet(urlPatterns = {"/api/clientes", "/api/login", "/api/logout", "/api/perfil"})
public class ApiContaServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object get(HttpServletRequest request, HttpServletResponse response) {
        if (!"/api/perfil".equals(request.getServletPath())) {
            throw new ApiErro(405, "Método não permitido.");
        }
        return new ApiConversor(request).cliente(exigirCliente(request));
    }

    @Override
    protected Object post(HttpServletRequest request, HttpServletResponse response) throws Exception {
        switch (request.getServletPath()) {
            case "/api/login" -> {
                return login(request);
            }
            case "/api/logout" -> {
                TokenService.revogar(token(request));
                return Map.of("mensagem", "Você saiu da sua conta.");
            }
            case "/api/clientes" -> {
                Object resultado = cadastrar(request);
                response.setStatus(HttpServletResponse.SC_CREATED);
                return resultado;
            }
            default -> throw new ApiErro(405, "Método não permitido.");
        }
    }

    private Object login(HttpServletRequest request) throws Exception {
        Map<String, Object> dados = lerCorpo(request);
        Cliente cliente = clienteService.autenticar(texto(dados, "email"), texto(dados, "senha"));
        if (cliente == null) {
            // Mesma mensagem para e-mail ou senha errados (não revela quais e-mails existem).
            throw new ApiErro(401, "E-mail ou senha inválidos.");
        }
        return sessao(request, clienteService.buscarPerfil(cliente.getIdCliente()));
    }

    /**
     * Corpo esperado:
     * { "nome", "email", "cpf", "telefone", "senha", "confirmacao",
     *   "endereco": { "cep", "logradouro", "numero", "complemento", "bairro", "cidade", "estado" } }
     */
    private Object cadastrar(HttpServletRequest request) throws Exception {
        Map<String, Object> dados = lerCorpo(request);

        Object enderecoBruto = dados.get("endereco");
        @SuppressWarnings("unchecked")
        Map<String, Object> e = enderecoBruto instanceof Map<?, ?> ? (Map<String, Object>) enderecoBruto : Map.of();

        Cliente cliente = new Cliente(texto(dados, "nome"), texto(dados, "email"), null,
                texto(dados, "cpf"), texto(dados, "telefone"));
        Endereco endereco = new Endereco(0, texto(e, "cep"), texto(e, "logradouro"), texto(e, "numero"),
                texto(e, "complemento"), texto(e, "bairro"), texto(e, "cidade"), texto(e, "estado"));

        // As mesmas regras do cadastro do site (ValidacaoException vira 400 com os campos).
        Cliente cadastrado = clienteService.cadastrar(cliente, texto(dados, "senha"),
                texto(dados, "confirmacao"), endereco);
        return sessao(request, clienteService.buscarPerfil(cadastrado.getIdCliente()));
    }

    /** Resposta de login/cadastro: token + dados do cliente. */
    private Map<String, Object> sessao(HttpServletRequest request, Cliente cliente) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("token", TokenService.gerar(cliente.getIdCliente()));
        m.put("validadeDias", TokenService.VALIDADE_MS / (24 * 60 * 60 * 1000));
        m.put("cliente", new ApiConversor(request).cliente(cliente));
        return m;
    }
}
