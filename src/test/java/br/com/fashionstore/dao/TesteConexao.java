package br.com.fashionstore.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import br.com.fashionstore.util.ConnectionFactory;

/**
 * Testa SOMENTE a conexão com o MySQL (Etapa 3).
 * Não grava nada no banco.
 *
 * Executar: Ctrl+Shift+P > Tasks: Run Task > "Teste: conexão com o banco"
 */
public class TesteConexao {

    public static void main(String[] args) {
        System.out.println("=== Teste de conexão - FashionStore ===\n");

        try {
            System.out.println("URL: " + ConnectionFactory.getUrl());
        } catch (IllegalStateException e) {
            System.out.println("[CONFIGURAÇÃO] " + e.getMessage());
            System.exit(1);
        }

        try (Connection con = ConnectionFactory.getConnection()) {
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("[OK] Conectado!");
            System.out.println("     Banco:   " + con.getCatalog());
            System.out.println("     Servidor: " + meta.getDatabaseProductName() + " "
                    + meta.getDatabaseProductVersion());
            System.out.println("     Usuário: " + meta.getUserName());

        } catch (SQLException e) {
            System.out.println("[FALHOU] Não foi possível conectar.");
            System.out.println("     Código MySQL: " + e.getErrorCode());
            System.out.println("     Mensagem:     " + e.getMessage());
            System.out.println("\nDicas:");
            System.out.println(" - 'Communications link failure': o MySQL está desligado ou host/porta errados.");
            System.out.println(" - 'Access denied': usuário ou senha errados no db.properties.");
            System.out.println(" - 'Unknown database': o banco fashionstore ainda não foi criado.");
            System.exit(1);
        }
    }
}
