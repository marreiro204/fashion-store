package br.com.fashionstore.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Situações possíveis de um pedido.
 *
 * A documentação define a coluna "status VARCHAR" sem dizer quais valores
 * ela aceita. Este enum fixa esses valores para que a Web, o Flutter e o
 * banco usem sempre os mesmos textos.
 *
 * Caminho permitido (o admin não pode "pular" nem voltar etapas):
 *
 *   PENDENTE ──> PAGO ──> ENVIADO ──> ENTREGUE
 *      │           │
 *      └───────────┴──> CANCELADO   (devolve as peças ao estoque)
 *
 * ENTREGUE e CANCELADO são finais.
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

    /** A mudança deste status para o "novo" é permitida? */
    public boolean podeMudarPara(StatusPedido novo) {
        return switch (this) {
            case PENDENTE -> novo == PAGO || novo == CANCELADO;
            case PAGO -> novo == ENVIADO || novo == CANCELADO;
            case ENVIADO -> novo == ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
    }

    /** Próximos status possíveis (para montar o seletor do admin). Na JSP: ${pedido.status.proximos} */
    public List<StatusPedido> getProximos() {
        List<StatusPedido> proximos = new ArrayList<>();
        for (StatusPedido s : values()) {
            if (podeMudarPara(s)) {
                proximos.add(s);
            }
        }
        return proximos;
    }

    /** Converte texto ("pago", "PAGO") em status; texto inválido -> null. */
    public static StatusPedido de(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
