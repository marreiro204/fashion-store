package br.com.fashionstore.service;

import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.dao.ClienteDAO;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.model.Perfil;
import br.com.fashionstore.util.SenhaUtil;
import br.com.fashionstore.util.Validador;

/**
 * Teste do cadastro e do login (Etapa 4) SEM banco de dados.
 *
 * Como? Criamos um ClienteDAO "falso" que guarda os clientes numa lista
 * em memória, e entregamos ao ClienteService. O Service não percebe a
 * diferença, então testamos todas as regras (validação, hash, e-mail
 * repetido, login) sem precisar do MySQL.
 *
 * Executar: Ctrl+Shift+P > Tasks: Run Task > "Teste: autenticação"
 */
public class TesteAutenticacao {

    private static int ok = 0;
    private static int falhas = 0;

    /** DAO falso: mesma "cara" do ClienteDAO, mas grava numa lista. */
    static class ClienteDAOEmMemoria extends ClienteDAO {
        final List<Cliente> clientes = new ArrayList<>();

        @Override
        public Cliente inserir(Cliente cliente) {
            cliente.setIdCliente(clientes.size() + 1);
            Cliente copia = new Cliente(cliente.getNome(), cliente.getEmail(), cliente.getSenha(),
                    cliente.getCpf(), cliente.getTelefone());
            copia.setIdCliente(cliente.getIdCliente());
            copia.setPerfil(cliente.getPerfil());
            clientes.add(copia);
            return cliente;
        }

        @Override
        public Cliente buscarPorEmail(String email) {
            for (Cliente c : clientes) {
                if (c.getEmail().equals(email)) {
                    Cliente copia = new Cliente(c.getNome(), c.getEmail(), c.getSenha(), c.getCpf(), c.getTelefone());
                    copia.setIdCliente(c.getIdCliente());
                    copia.setPerfil(c.getPerfil());
                    return copia;
                }
            }
            return null;
        }

        @Override
        public boolean existeEmail(String email) {
            return buscarPorEmail(email) != null;
        }

        @Override
        public boolean existeCpf(String cpf) {
            return clientes.stream().anyMatch(c -> c.getCpf().equals(cpf));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Teste de cadastro e login - FashionStore ===\n");

        // ---------------- Validador ----------------
        System.out.println("-- Validador");
        verificar("CPF válido (529.982.247-25)", Validador.cpfValido(Validador.somenteDigitos("529.982.247-25")));
        verificar("CPF com dígito errado é inválido", !Validador.cpfValido("52998224726"));
        verificar("CPF 111.111.111-11 é inválido", !Validador.cpfValido("11111111111"));
        verificar("E-mail válido", Validador.emailValido("ana@email.com"));
        verificar("E-mail sem @ é inválido", !Validador.emailValido("ana.email.com"));
        verificar("Senha 'abc12345' é aceita", Validador.senhaForte("abc12345"));
        verificar("Senha só com letras é recusada", !Validador.senhaForte("abcdefgh"));
        verificar("Senha curta é recusada", !Validador.senhaForte("ab12"));
        verificar("CEP 01000-000 é válido", Validador.cepValido(Validador.somenteDigitos("01000-000")));
        verificar("UF 'sp' é válida", Validador.ufValida("sp"));
        verificar("UF 'XX' é inválida", !Validador.ufValida("XX"));

        // ---------------- SenhaUtil (BCrypt) ----------------
        System.out.println("-- Hash de senha");
        String hash = SenhaUtil.gerarHash("Segredo123");
        verificar("Hash tem 60 caracteres e começa com $2a$", hash.length() == 60 && hash.startsWith("$2a$"));
        verificar("Hash não contém a senha", !hash.contains("Segredo123"));
        verificar("Mesma senha gera hashes diferentes (sal)", !hash.equals(SenhaUtil.gerarHash("Segredo123")));
        verificar("Senha correta confere", SenhaUtil.conferir("Segredo123", hash));
        verificar("Senha errada não confere", !SenhaUtil.conferir("segredo123", hash));
        verificar("Hash do admin de teste (guia do banco) confere com Admin@123",
                SenhaUtil.conferir("Admin@123", "$2a$10$9FzfukjVHIVuo4jOP4D9Ku9hQhqlyYK1WJQHonrjf3P24VJNTAGzm"));

        // ---------------- Cadastro ----------------
        System.out.println("-- Cadastro");
        ClienteDAOEmMemoria dao = new ClienteDAOEmMemoria();
        ClienteService service = new ClienteService(dao);

        Cliente ana = null;
        try {
            ana = service.cadastrar(
                    new Cliente("  Ana Souza  ", "ANA@Email.com", null, "529.982.247-25", "(11) 98888-7777"),
                    "Segredo123", "Segredo123",
                    new Endereco(0, "01000-000", "Rua das Flores", "100", "", "Centro", "São Paulo", "sp"));
        } catch (ValidacaoException e) {
            System.out.println("   erros inesperados: " + e.getErros());
        }
        verificar("Cadastro válido é aceito", ana != null && ana.getIdCliente() == 1);
        Cliente gravado = dao.clientes.get(0);
        verificar("Nome sem espaços extras", "Ana Souza".equals(gravado.getNome()));
        verificar("E-mail em minúsculas", "ana@email.com".equals(gravado.getEmail()));
        verificar("CPF e telefone gravados só com números",
                "52998224725".equals(gravado.getCpf()) && "11988887777".equals(gravado.getTelefone()));
        verificar("Senha gravada como hash BCrypt", gravado.getSenha().startsWith("$2a$") && !gravado.getSenha().equals("Segredo123"));
        verificar("Cadastro público sempre cria perfil CLIENTE", gravado.getPerfil() == Perfil.CLIENTE);
        verificar("Objeto devolvido não carrega a senha", ana != null && ana.getSenha() == null);
        verificar("Estado convertido para maiúsculas (SP)", ana != null && "SP".equals(ana.getEnderecos().get(0).getEstado()));

        verificar("E-mail repetido é recusado", campoComErro(service,
                new Cliente("Outra Pessoa", "ana@email.com", null, "11144477735", null), "Segredo123", "Segredo123", "email"));
        verificar("CPF repetido é recusado", campoComErro(service,
                new Cliente("Outra Pessoa", "outra@email.com", null, "52998224725", null), "Segredo123", "Segredo123", "cpf"));
        verificar("Senhas diferentes são recusadas", campoComErro(service,
                new Cliente("Outra Pessoa", "outra@email.com", null, "11144477735", null), "Segredo123", "Segredo999", "confirmacao"));
        verificar("CPF inválido é recusado", campoComErro(service,
                new Cliente("Outra Pessoa", "outra@email.com", null, "12345678900", null), "Segredo123", "Segredo123", "cpf"));
        verificar("Senha fraca é recusada", campoComErro(service,
                new Cliente("Outra Pessoa", "outra@email.com", null, "11144477735", null), "12345678", "12345678", "senha"));
        verificar("Nada foi gravado nas tentativas inválidas", dao.clientes.size() == 1);

        // ---------------- Login ----------------
        System.out.println("-- Login");
        Cliente logado = service.autenticar("ana@email.com", "Segredo123");
        verificar("Login com dados corretos", logado != null && logado.getIdCliente() == 1);
        verificar("Cliente logado não carrega a senha", logado != null && logado.getSenha() == null);
        verificar("Login aceita e-mail com maiúsculas/espaços", service.autenticar("  ANA@EMAIL.COM ", "Segredo123") != null);
        verificar("Senha errada é recusada", service.autenticar("ana@email.com", "Errada123") == null);
        verificar("E-mail inexistente é recusado", service.autenticar("ninguem@email.com", "Segredo123") == null);
        verificar("Campos vazios são recusados", service.autenticar("", "") == null);

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    /** Tenta cadastrar e confere se o erro apareceu no campo esperado. */
    private static boolean campoComErro(ClienteService service, Cliente cliente,
                                        String senha, String confirmacao, String campo) {
        try {
            service.cadastrar(cliente, senha, confirmacao,
                    new Endereco(0, "01000000", "Rua A", "1", null, "Centro", "São Paulo", "SP"));
            return false;
        } catch (ValidacaoException e) {
            return e.getErros().containsKey(campo);
        }
    }

    private static void verificar(String descricao, boolean condicao) {
        if (condicao) {
            ok++;
            System.out.println("[OK]      " + descricao);
        } else {
            falhas++;
            System.out.println("[FALHOU]  " + descricao);
        }
    }
}
