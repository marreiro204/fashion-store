package br.com.fashionstore.dao;

import java.sql.SQLException;

/**
 * Erro ao acessar o banco de dados.
 *
 * Os DAOs capturam a SQLException (que é técnica e "checked") e lançam
 * esta exceção, que:
 * - é "unchecked" (RuntimeException): os Servlets não precisam de
 *   try/catch em toda chamada, só onde querem tratar o erro;
 * - informa o MOTIVO de forma simples, para a tela mostrar uma
 *   mensagem amigável (ex.: "E-mail já cadastrado").
 *
 * A SQLException original fica guardada como "causa" para aparecer no log.
 */
public class DAOException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Motivos que a aplicação sabe tratar. */
    public enum Motivo {
        /** UNIQUE violado: e-mail, CPF, nome de categoria ou produto+tamanho repetido. */
        DUPLICADO,
        /** Exclusão bloqueada: o registro é usado em outra tabela (RESTRICT). */
        EM_USO,
        /** Chave estrangeira aponta para um registro que não existe. */
        REFERENCIA_INVALIDA,
        /** Regra CHECK violada (ex.: status inválido, quantidade negativa). */
        REGRA_VIOLADA,
        /** Não há estoque suficiente para o tamanho pedido. */
        ESTOQUE_INSUFICIENTE,
        /** Qualquer outro problema (banco fora do ar, senha errada, SQL com erro...). */
        ERRO_BANCO
    }

    private final Motivo motivo;

    public DAOException(Motivo motivo, String mensagem) {
        super(mensagem);
        this.motivo = motivo;
    }

    public DAOException(Motivo motivo, String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }

    /**
     * Converte uma SQLException em DAOException, identificando o motivo
     * pelo código de erro do MySQL.
     *
     * @param operacao descrição do que se tentava fazer (ex.: "inserir categoria")
     */
    public static DAOException de(String operacao, SQLException e) {
        Motivo motivo = switch (e.getErrorCode()) {
            case 1062 -> Motivo.DUPLICADO;                 // Duplicate entry
            case 1451 -> Motivo.EM_USO;                    // Cannot delete or update a parent row
            case 1452 -> Motivo.REFERENCIA_INVALIDA;       // Cannot add or update a child row
            case 3819 -> Motivo.REGRA_VIOLADA;             // Check constraint is violated
            default -> Motivo.ERRO_BANCO;
        };
        return new DAOException(motivo, "Erro ao " + operacao + ": " + e.getMessage(), e);
    }
}
