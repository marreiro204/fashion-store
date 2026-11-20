package br.com.fashionstore.api;

import java.util.Map;

/**
 * Erro que vira uma resposta JSON da API, com o código HTTP certo.
 *
 *   throw new ApiErro(404, "Produto não encontrado.");
 *   -> HTTP 404  {"erro": "Produto não encontrado."}
 */
public class ApiErro extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final Map<String, String> campos;

    public ApiErro(int status, String mensagem) {
        this(status, mensagem, null);
    }

    /** Com erros por campo (ex.: {"email": "Este e-mail já está cadastrado."}). */
    public ApiErro(int status, String mensagem, Map<String, String> campos) {
        super(mensagem);
        this.status = status;
        this.campos = campos;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}
