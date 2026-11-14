package br.com.fashionstore.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dados digitados inválidos.
 *
 * Guarda os erros por CAMPO, para a página mostrar cada mensagem ao lado
 * do campo certo. Ex.: {"email" -> "E-mail já cadastrado."}
 * Na JSP: ${erros.email}
 *
 * É uma exceção "checked" (extends Exception): o compilador obriga quem
 * chama o Service a tratar o caso de dados inválidos.
 */
public class ValidacaoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> erros;

    public ValidacaoException(Map<String, String> erros) {
        super("Dados inválidos: " + erros);
        this.erros = Collections.unmodifiableMap(new LinkedHashMap<>(erros));
    }

    /** Atalho para um único erro. */
    public ValidacaoException(String campo, String mensagem) {
        this(Map.of(campo, mensagem));
    }

    public Map<String, String> getErros() {
        return erros;
    }
}
