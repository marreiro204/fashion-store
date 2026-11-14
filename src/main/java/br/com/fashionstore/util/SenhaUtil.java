package br.com.fashionstore.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hash e verificação de senhas com BCrypt.
 *
 * Por que hash e não guardar a senha?
 * Se o banco vazar, ninguém consegue "desfazer" o hash para descobrir a
 * senha original. No login, não "descriptografamos" nada: geramos o hash
 * da senha digitada e comparamos.
 *
 * Por que BCrypt?
 * - É lento de propósito (dificulta tentar milhões de senhas);
 * - Gera um "sal" aleatório para cada senha: duas pessoas com a mesma
 *   senha terão hashes diferentes.
 *
 * O hash tem 60 caracteres e já contém o sal e o custo. Exemplo:
 *   $2a$10$9FzfukjVHIVuo4jOP4D9Ku9hQhqlyYK1WJQHonrjf3P24VJNTAGzm
 */
public final class SenhaUtil {

    /** Custo 10 = 2^10 rodadas. Bom equilíbrio entre segurança e tempo (~0,1 s). */
    private static final int CUSTO = 10;

    /**
     * Hash usado quando o e-mail não existe no login. Verificar contra ele
     * faz o tempo de resposta ser parecido com o de um e-mail existente,
     * dificultando descobrir quais e-mails estão cadastrados.
     */
    private static final String HASH_FICTICIO = BCrypt.hashpw("senha-ficticia", BCrypt.gensalt(CUSTO));

    private SenhaUtil() {
    }

    /** Gera o hash para gravar no banco. */
    public static String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(CUSTO));
    }

    /**
     * Confere se a senha digitada corresponde ao hash gravado.
     * Se o hash for nulo (usuário inexistente), compara com um hash
     * fictício e devolve false, gastando o mesmo tempo.
     */
    public static boolean conferir(String senhaDigitada, String hashGravado) {
        if (senhaDigitada == null) {
            return false;
        }
        if (hashGravado == null || !hashGravado.startsWith("$2")) {
            BCrypt.checkpw(senhaDigitada, HASH_FICTICIO);
            return false;
        }
        try {
            return BCrypt.checkpw(senhaDigitada, hashGravado);
        } catch (IllegalArgumentException e) {
            return false; // hash corrompido no banco
        }
    }
}
