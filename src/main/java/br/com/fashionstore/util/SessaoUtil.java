package br.com.fashionstore.util;

import br.com.fashionstore.model.Carrinho;
import br.com.fashionstore.model.Cliente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Centraliza o que guardamos na sessão do usuário.
 *
 * A sessão (HttpSession) é um "armário" no servidor, um por navegador.
 * O navegador só guarda um cookie com o número do armário (JSESSIONID).
 */
public final class SessaoUtil {

    /** Nome do atributo com o cliente logado. Nas JSPs: ${sessionScope.usuarioLogado} */
    public static final String USUARIO_LOGADO = "usuarioLogado";

    /** Mensagem exibida uma única vez na próxima página (ex.: "Cadastro realizado"). */
    public static final String MENSAGEM = "mensagem";

    /** Nome do atributo com o carrinho. Nas JSPs: ${sessionScope.carrinho} */
    public static final String CARRINHO = "carrinho";

    private SessaoUtil() {
    }

    /** Devolve o carrinho da sessão, criando um vazio se ainda não existir. */
    public static Carrinho getCarrinho(HttpServletRequest request) {
        HttpSession sessao = request.getSession(true);
        Carrinho carrinho = (Carrinho) sessao.getAttribute(CARRINHO);
        if (carrinho == null) {
            carrinho = new Carrinho();
            sessao.setAttribute(CARRINHO, carrinho);
        }
        return carrinho;
    }

    /** @return o cliente logado, ou null se ninguém estiver logado */
    public static Cliente getUsuarioLogado(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false); // false: não cria sessão à toa
        return sessao == null ? null : (Cliente) sessao.getAttribute(USUARIO_LOGADO);
    }

    /**
     * Registra o login.
     *
     * changeSessionId() troca o número da sessão. Isso evita o ataque
     * "session fixation" (alguém que conhecia o número antigo não ganha
     * acesso à conta). O conteúdo da sessão (ex.: carrinho) é mantido.
     */
    public static void login(HttpServletRequest request, Cliente cliente) {
        request.getSession(true);
        request.changeSessionId();
        cliente.setSenha(null); // o hash nunca fica na sessão
        request.getSession().setAttribute(USUARIO_LOGADO, cliente);
    }

    /** Encerra a sessão inteira (usuário, carrinho, tudo). */
    public static void logout(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
    }

    /** Guarda uma mensagem para mostrar na próxima página. */
    public static void mensagem(HttpServletRequest request, String texto) {
        request.getSession(true).setAttribute(MENSAGEM, texto);
    }
}
