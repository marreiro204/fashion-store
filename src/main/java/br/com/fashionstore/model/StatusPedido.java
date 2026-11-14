package br.com.fashionstore.model;

/**
 * Situações possíveis de um pedido.
 *
 * A documentação define a coluna "status VARCHAR" sem dizer quais valores
 * ela aceita. Este enum fixa esses valores para que a Web, o Flutter e o
 * banco usem sempre os mesmos textos.
 *
 * Cada constante tem uma "descricao" amigável para exibir nas telas.
 */
public enum StatusPedido {

    PENDENTE("Pendente"),
    PAGO("Pago"),
    ENVIADO("Enviado"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String descricao;

    // Construtor de enum é sempre privado: só as constantes acima o usam.
    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Um pedido entregue ou cancelado não muda mais de status.
     */
    public boolean isFinal() {
        return this == ENTREGUE || this == CANCELADO;
    }
}
