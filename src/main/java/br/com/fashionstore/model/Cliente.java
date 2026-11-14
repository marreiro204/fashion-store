package br.com.fashionstore.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente (e também administrador) da loja.
 * Tabela: cliente (id_cliente, nome, email, senha, cpf, telefone, perfil).
 *
 * Ajuste aprovado (Proposta 2): campo "perfil" para diferenciar
 * CLIENTE de ADMIN, usado no controle de acesso.
 *
 * "implements Serializable": o cliente logado ficará guardado na sessão
 * (HttpSession). Serializable permite ao Tomcat salvar a sessão em disco
 * quando é reiniciado, sem perder o login.
 */
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idCliente;
    private String nome;
    private String email;
    private String senha;        // guarda o HASH BCrypt, nunca a senha em texto puro
    private String cpf;
    private String telefone;
    private Perfil perfil = Perfil.CLIENTE; // todo cadastro novo é CLIENTE

    // Relacionamento 1:N -> um cliente pode ter vários endereços
    private List<Endereco> enderecos = new ArrayList<>();

    public Cliente() {
    }

    public Cliente(String nome, String email, String senha, String cpf, String telefone) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    // ===== Métodos de negócio =====

    /** Usado para liberar ou bloquear a área administrativa. */
    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    /** Adiciona um endereço e já liga o endereço a este cliente. */
    public void adicionarEndereco(Endereco endereco) {
        endereco.setIdCliente(this.idCliente);
        enderecos.add(endereco);
    }

    // ===== Getters e setters =====

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public List<Endereco> getEnderecos() {
        return enderecos;
    }

    public void setEnderecos(List<Endereco> enderecos) {
        this.enderecos = enderecos;
    }

    /** Não inclui a senha: toString aparece em logs e não deve expor dados sensíveis. */
    @Override
    public String toString() {
        return "Cliente{id=" + idCliente + ", nome='" + nome + "', email='" + email
                + "', perfil=" + perfil + "}";
    }
}
