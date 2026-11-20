package br.com.fashionstore.util;

import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Leitura e escrita de JSON (o formato que a API usa para conversar com o
 * app Flutter), feita só com Java puro, sem biblioteca extra.
 *
 * ESCREVER: Json.escrever(objeto) aceita
 *   null, String, Number (inclusive BigDecimal), Boolean, enum, datas (java.time),
 *   Map (vira objeto {...}) e Collection/array (vira lista [...]).
 *
 * LER: Json.ler(texto) devolve
 *   Map<String,Object> para {...}, List<Object> para [...], String,
 *   BigDecimal para números, Boolean ou null.
 *   Texto inválido -> IllegalArgumentException com a posição do erro.
 */
public final class Json {

    private Json() {
    }

    // ============================== Escrita ==============================

    public static String escrever(Object valor) {
        StringBuilder sb = new StringBuilder();
        escrever(valor, sb);
        return sb.toString();
    }

    private static void escrever(Object valor, StringBuilder sb) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof String s) {
            escreverTexto(s, sb);
        } else if (valor instanceof BigDecimal d) {
            sb.append(d.toPlainString());              // 189.90 (sem notação científica)
        } else if (valor instanceof Number || valor instanceof Boolean) {
            sb.append(valor);
        } else if (valor instanceof Enum<?> e) {
            escreverTexto(e.name(), sb);               // StatusPedido.PAGO -> "PAGO"
        } else if (valor instanceof TemporalAccessor) {
            escreverTexto(valor.toString(), sb);       // 2026-11-14T20:20
        } else if (valor instanceof Map<?, ?> mapa) {
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<?, ?> e : mapa.entrySet()) {
                if (!primeiro) {
                    sb.append(',');
                }
                escreverTexto(String.valueOf(e.getKey()), sb);
                sb.append(':');
                escrever(e.getValue(), sb);
                primeiro = false;
            }
            sb.append('}');
        } else if (valor instanceof Collection<?> lista) {
            sb.append('[');
            boolean primeiro = true;
            for (Object item : lista) {
                if (!primeiro) {
                    sb.append(',');
                }
                escrever(item, sb);
                primeiro = false;
            }
            sb.append(']');
        } else if (valor instanceof Object[] array) {
            escrever(List.of(array), sb);
        } else {
            throw new IllegalArgumentException("Tipo não suportado em JSON: " + valor.getClass().getName());
        }
    }

    /** Texto entre aspas, escapando os caracteres especiais. */
    private static void escreverTexto(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    // Controles e "<" (evita "</script>" se o JSON for embutido numa página)
                    if (c < 0x20 || c == '<' || c == 0x2028 || c == 0x2029) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    // =============================== Leitura ===============================

    /** Limite de profundidade (evita JSON malicioso com milhares de níveis). */
    private static final int PROFUNDIDADE_MAXIMA = 32;

    public static Object ler(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("JSON vazio.");
        }
        Leitor leitor = new Leitor(texto);
        leitor.pularEspacos();
        Object valor = leitor.lerValor(0);
        leitor.pularEspacos();
        if (!leitor.fim()) {
            throw leitor.erro("conteúdo depois do fim do JSON");
        }
        return valor;
    }

    /** Lê e exige um objeto {...}. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> lerObjeto(String texto) {
        Object valor = ler(texto);
        if (!(valor instanceof Map)) {
            throw new IllegalArgumentException("Era esperado um objeto JSON {...}.");
        }
        return (Map<String, Object>) valor;
    }

    private static final class Leitor {
        private final String s;
        private int pos;

        Leitor(String s) {
            this.s = s;
        }

        boolean fim() {
            return pos >= s.length();
        }

        IllegalArgumentException erro(String motivo) {
            return new IllegalArgumentException("JSON inválido na posição " + pos + ": " + motivo);
        }

        void pularEspacos() {
            while (!fim() && Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
        }

        Object lerValor(int profundidade) {
            if (profundidade > PROFUNDIDADE_MAXIMA) {
                throw erro("muitos níveis aninhados");
            }
            if (fim()) {
                throw erro("fim inesperado");
            }
            char c = s.charAt(pos);
            return switch (c) {
                case '{' -> lerObjeto(profundidade);
                case '[' -> lerLista(profundidade);
                case '"' -> lerTexto();
                case 't' -> palavra("true", Boolean.TRUE);
                case 'f' -> palavra("false", Boolean.FALSE);
                case 'n' -> palavra("null", null);
                default -> {
                    if (c == '-' || Character.isDigit(c)) {
                        yield lerNumero();
                    }
                    throw erro("caractere inesperado '" + c + "'");
                }
            };
        }

        Map<String, Object> lerObjeto(int profundidade) {
            Map<String, Object> mapa = new LinkedHashMap<>();
            pos++; // {
            pularEspacos();
            if (!fim() && s.charAt(pos) == '}') {
                pos++;
                return mapa;
            }
            while (true) {
                pularEspacos();
                if (fim() || s.charAt(pos) != '"') {
                    throw erro("nome de campo entre aspas esperado");
                }
                String chave = lerTexto();
                pularEspacos();
                esperar(':');
                pularEspacos();
                mapa.put(chave, lerValor(profundidade + 1));
                pularEspacos();
                if (fim()) {
                    throw erro("'}' esperado");
                }
                char c = s.charAt(pos++);
                if (c == '}') {
                    return mapa;
                }
                if (c != ',') {
                    throw erro("',' ou '}' esperado");
                }
            }
        }

        List<Object> lerLista(int profundidade) {
            List<Object> lista = new ArrayList<>();
            pos++; // [
            pularEspacos();
            if (!fim() && s.charAt(pos) == ']') {
                pos++;
                return lista;
            }
            while (true) {
                pularEspacos();
                lista.add(lerValor(profundidade + 1));
                pularEspacos();
                if (fim()) {
                    throw erro("']' esperado");
                }
                char c = s.charAt(pos++);
                if (c == ']') {
                    return lista;
                }
                if (c != ',') {
                    throw erro("',' ou ']' esperado");
                }
            }
        }

        String lerTexto() {
            pos++; // "
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (fim()) {
                    throw erro("texto sem aspas de fechamento");
                }
                char c = s.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (fim()) {
                        throw erro("escape incompleto");
                    }
                    char e = s.charAt(pos++);
                    switch (e) {
                        case '"', '\\', '/' -> sb.append(e);
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> {
                            if (pos + 4 > s.length()) {
                                throw erro("\\u incompleto");
                            }
                            try {
                                sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                            } catch (NumberFormatException ex) {
                                throw erro("\\u inválido");
                            }
                            pos += 4;
                        }
                        default -> throw erro("escape desconhecido \\" + e);
                    }
                } else if (c < 0x20) {
                    throw erro("caractere de controle dentro do texto");
                } else {
                    sb.append(c);
                }
            }
        }

        BigDecimal lerNumero() {
            int inicio = pos;
            if (s.charAt(pos) == '-') {
                pos++;
            }
            while (!fim() && "0123456789.eE+-".indexOf(s.charAt(pos)) >= 0) {
                pos++;
            }
            String numero = s.substring(inicio, pos);
            if (!numero.matches("-?(0|[1-9]\\d*)(\\.\\d+)?([eE][+-]?\\d+)?")) {
                throw erro("número inválido '" + numero + "'");
            }
            return new BigDecimal(numero);
        }

        Object palavra(String esperada, Object valor) {
            if (!s.startsWith(esperada, pos)) {
                throw erro("valor inválido");
            }
            pos += esperada.length();
            return valor;
        }

        void esperar(char c) {
            if (fim() || s.charAt(pos) != c) {
                throw erro("'" + c + "' esperado");
            }
            pos++;
        }
    }
}
