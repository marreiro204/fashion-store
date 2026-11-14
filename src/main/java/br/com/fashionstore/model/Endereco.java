package br.com.fashionstore.model;

import java.io.Serializable;

/**
 * Endereço de entrega de um cliente.
 * Tabela: endereco (id_endereco, id_cliente, cep, logradouro, numero,
 *                   complemento, bairro, cidade, estado).
 *
 * Relacionamento: guardamos só o idCliente (e não o objeto Cliente inteiro),
 * porque o Cliente já guarda a lista de endereços. Se os dois lados
 * apontassem um para o outro, teríamos um "ciclo", que atrapalha na hora de
 * gerar JSON para o Flutter.
 */
public class Endereco implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idEndereco;
    private int idCliente;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado; // sigla com 2 letras: SP, RJ...

    public Endereco() {
    }

    public Endereco(int idCliente, String cep, String logradouro, String numero,
                    String complemento, String bairro, String cidade, String estado) {
        this.idCliente = idCliente;
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
    }

    /**
     * Endereço em uma linha, para exibir nas páginas.
     * Ex.: "Rua A, 100 - Apto 2 - Centro - São Paulo/SP - CEP 01000-000"
     */
    public String getEnderecoFormatado() {
        StringBuilder sb = new StringBuilder();
        sb.append(logradouro).append(", ").append(numero);
        if (complemento != null && !complemento.isBlank()) {
            sb.append(" - ").append(complemento);
        }
        sb.append(" - ").append(bairro)
          .append(" - ").append(cidade).append("/").append(estado)
          .append(" - CEP ").append(cep);
        return sb.toString();
    }

    public int getIdEndereco() {
        return idEndereco;
    }

    public void setIdEndereco(int idEndereco) {
        this.idEndereco = idEndereco;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Endereco{id=" + idEndereco + ", " + getEnderecoFormatado() + "}";
    }
}
