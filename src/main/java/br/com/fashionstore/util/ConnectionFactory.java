package br.com.fashionstore.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexões JDBC com o MySQL.
 *
 * É o ÚNICO lugar do sistema que sabe como se conectar ao banco.
 * Todos os DAOs pedem conexões aqui:
 *
 *     try (Connection con = ConnectionFactory.getConnection()) { ... }
 *
 * Os dados de acesso (host, porta, banco, usuário e senha) NÃO ficam no
 * código: são lidos do arquivo src/main/resources/db.properties, que não
 * vai para o GitHub. Veja o modelo em db.properties.example.
 *
 * "final" + construtor privado = ninguém cria objetos desta classe;
 * ela só tem métodos estáticos.
 */
public final class ConnectionFactory {

    private static final String ARQUIVO_CONFIG = "db.properties";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Parâmetros extras da URL JDBC:
     * - useSSL=false: conexão local, sem certificado (ambiente acadêmico);
     * - allowPublicKeyRetrieval=true: necessário no MySQL 8 sem SSL;
     * - serverTimezone: evita erro de fuso horário nas datas;
     * - characterEncoding=UTF-8: acentos corretos.
     * Pode ser trocado pela chave opcional "db.params" no db.properties.
     */
    private static final String PARAMETROS_PADRAO =
            "useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8";

    // Guardamos a URL e as credenciais depois de ler o arquivo uma vez.
    private static String url;
    private static String usuario;
    private static String senha;

    private ConnectionFactory() {
    }

    /**
     * Abre uma NOVA conexão com o banco.
     * Quem chama é responsável por fechá-la (use try-with-resources).
     *
     * @throws SQLException se o db.properties estiver faltando ou incompleto,
     *                      o banco estiver fora do ar, a senha estiver errada,
     *                      o banco não existir etc.
     */
    public static Connection getConnection() throws SQLException {
        try {
            carregarConfiguracao();
        } catch (IllegalStateException e) {
            // Problema de configuração vira SQLException: os DAOs já sabem tratá-la
            // e a tela mostra uma mensagem amigável em vez de um erro 500.
            throw new SQLException(e.getMessage(), e);
        }
        return DriverManager.getConnection(url, usuario, senha);
    }

    /** URL usada na conexão (sem a senha). Útil para mensagens de erro e testes. */
    public static String getUrl() {
        carregarConfiguracao();
        return url;
    }

    /**
     * Lê o db.properties apenas na primeira vez.
     * "synchronized" evita que duas requisições simultâneas leiam ao mesmo tempo.
     */
    private static synchronized void carregarConfiguracao() {
        if (url != null) {
            return; // já carregado
        }

        Properties props = new Properties();
        try (InputStream in = ConnectionFactory.class.getClassLoader()
                .getResourceAsStream(ARQUIVO_CONFIG)) {

            if (in == null) {
                throw new IllegalStateException(
                        "Arquivo " + ARQUIVO_CONFIG + " não encontrado. Copie "
                        + "src/main/resources/db.properties.example para "
                        + "src/main/resources/db.properties e preencha os dados do banco.");
            }
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o " + ARQUIVO_CONFIG, e);
        }

        String host = obrigatorio(props, "db.host");
        String porta = obrigatorio(props, "db.port");
        String banco = obrigatorio(props, "db.name");
        String user = obrigatorio(props, "db.user");
        // A senha pode estar vazia (ex.: MySQL local sem senha), mas a chave precisa existir.
        if (!props.containsKey("db.password")) {
            throw new IllegalStateException("Falta a chave db.password no " + ARQUIVO_CONFIG);
        }
        String parametros = props.getProperty("db.params", PARAMETROS_PADRAO).trim();

        // No Tomcat, o driver precisa ser carregado explicitamente.
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Driver do MySQL não encontrado. Confira a dependência mysql-connector-j no pom.xml.", e);
        }

        usuario = user;
        senha = props.getProperty("db.password").trim();
        url = "jdbc:mysql://" + host + ":" + porta + "/" + banco + "?" + parametros;
    }

    private static String obrigatorio(Properties props, String chave) {
        String valor = props.getProperty(chave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta preencher " + chave + " no " + ARQUIVO_CONFIG);
        }
        return valor.trim();
    }
}
