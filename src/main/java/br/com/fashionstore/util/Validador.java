package br.com.fashionstore.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validações reutilizáveis dos dados digitados nos formulários.
 *
 * Validar no servidor é obrigatório: validações em HTML/JavaScript
 * ajudam o usuário, mas podem ser burladas facilmente.
 */
public final class Validador {

    /** Formato básico: algo@algo.algo, sem espaços. */
    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final Set<String> UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private Validador() {
    }

    public static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    /** Remove espaços do início e do fim; null vira "". */
    public static String limpar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    /** Mantém só os números: "123.456.789-09" -> "12345678909". */
    public static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean excede(String valor, int maximo) {
        return valor != null && valor.length() > maximo;
    }

    public static boolean emailValido(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    /** Pelo menos 8 caracteres, com letra e número. Máximo 72 (limite do BCrypt). */
    public static boolean senhaForte(String senha) {
        return senha != null
                && senha.length() >= 8
                && senha.length() <= 72
                && senha.chars().anyMatch(Character::isLetter)
                && senha.chars().anyMatch(Character::isDigit);
    }

    /** Telefone com DDD: 10 ou 11 dígitos. */
    public static boolean telefoneValido(String digitos) {
        return digitos != null && digitos.matches("\\d{10,11}");
    }

    public static boolean cepValido(String digitos) {
        return digitos != null && digitos.matches("\\d{8}");
    }

    public static boolean ufValida(String uf) {
        return uf != null && UFS.contains(uf.toUpperCase());
    }

    /** "12" -> 12; vazio ou texto inválido -> null (em vez de dar erro). */
    public static Integer converterInteiro(String valor) {
        if (vazio(valor)) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converte o preço digitado no formato brasileiro ou americano.
     *   "59,90" -> 59.90   |  "1.299,90" -> 1299.90  |  "R$ 59.90" -> 59.90
     * Texto inválido -> null.
     */
    public static BigDecimal converterPreco(String valor) {
        if (vazio(valor)) {
            return null;
        }
        String texto = valor.replace("R$", "").replace(" ", "").trim();
        if (texto.contains(",")) {
            texto = texto.replace(".", "").replace(",", "."); // formato brasileiro
        }
        if (!texto.matches("\\d+(\\.\\d{1,2})?")) {
            return null;
        }
        return new BigDecimal(texto).setScale(2, RoundingMode.UNNECESSARY);
    }

    /**
     * Imagem do produto: aceita só
     * - um nome de arquivo (ex.: camiseta-azul.jpg), procurado em assets/images/produtos/,
     *   opcionalmente dentro de UMA subpasta (ex.: masculino/camisa-social.jpg);
     * - ou um endereço http(s)://.
     * Recusa "javascript:...", "../../" e afins (segurança): o nome da subpasta
     * não pode ter ponto, então não há como "subir" de pasta.
     */
    public static boolean imagemValida(String imagem) {
        return imagem != null
                && (imagem.matches("([A-Za-z0-9][A-Za-z0-9_-]*/)?[A-Za-z0-9][A-Za-z0-9._-]*\\.(?i)(jpg|jpeg|png|webp|gif|svg)")
                    || imagem.matches("https?://[^\\s\"'<>]+"));
    }

    /**
     * Valida o CPF pelos dígitos verificadores (algoritmo oficial).
     * Recebe só números. Rejeita sequências como 11111111111.
     */
    public static boolean cpfValido(String digitos) {
        if (digitos == null || !digitos.matches("\\d{11}") || digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
                && digitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
    }

    /** Calcula o dígito verificador usando os "quantidade" primeiros dígitos. */
    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        int peso = quantidade + 1;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * peso--;
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
