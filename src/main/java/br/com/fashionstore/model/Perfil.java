package br.com.fashionstore.model;

/**
 * Perfil de acesso de um usuário do sistema (Proposta 2 aprovada).
 *
 * Um "enum" é um tipo com uma lista FIXA de valores possíveis.
 * Assim é impossível gravar um perfil errado como "admn" ou "cliente ".
 *
 * No banco, será salvo como texto: 'CLIENTE' ou 'ADMIN'.
 */
public enum Perfil {

    CLIENTE,
    ADMIN
}
