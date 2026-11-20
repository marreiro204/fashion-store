package br.com.fashionstore.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tokens de login da API (usados pelo app Flutter).
 *
 * O site guarda o login na SESSÃO (cookie). O app usa um TOKEN:
 * 1. No login, o servidor gera um código aleatório (o token) e devolve ao app;
 * 2. O app guarda o token e o envia em cada chamada, no cabeçalho
 *        Authorization: Bearer <token>
 * 3. O servidor procura o token aqui e descobre qual cliente está logado.
 *
 * Os tokens ficam em memória: valem por 7 dias ou até o logout. Se o Tomcat
 * for reiniciado, os tokens são perdidos e o app precisa fazer login de novo
 * (a API responde 401 e o app volta para a tela de login).
 */
public final class TokenService {

    /** Validade do token: 7 dias. */
    public static final long VALIDADE_MS = 7L * 24 * 60 * 60 * 1000;

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Map<String, Sessao> TOKENS = new ConcurrentHashMap<>();

    private record Sessao(int idCliente, long expiraEm) {
    }

    private TokenService() {
    }

    /** Gera um token novo para o cliente (32 bytes aleatórios = impossível de adivinhar). */
    public static String gerar(int idCliente) {
        limparExpirados();
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        TOKENS.put(token, new Sessao(idCliente, System.currentTimeMillis() + VALIDADE_MS));
        return token;
    }

    /** @return o id do cliente dono do token, ou null se o token não existir ou tiver expirado */
    public static Integer validar(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Sessao sessao = TOKENS.get(token);
        if (sessao == null) {
            return null;
        }
        if (sessao.expiraEm() < System.currentTimeMillis()) {
            TOKENS.remove(token);
            return null;
        }
        return sessao.idCliente();
    }

    /** Logout: o token deixa de valer. */
    public static void revogar(String token) {
        if (token != null) {
            TOKENS.remove(token);
        }
    }

    /** Usado só nos testes: força a expiração de um token. */
    static void expirarAgora(String token) {
        Sessao s = TOKENS.get(token);
        if (s != null) {
            TOKENS.put(token, new Sessao(s.idCliente(), System.currentTimeMillis() - 1));
        }
    }

    private static void limparExpirados() {
        long agora = System.currentTimeMillis();
        TOKENS.entrySet().removeIf(e -> e.getValue().expiraEm() < agora);
    }
}
