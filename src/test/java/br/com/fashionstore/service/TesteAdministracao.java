package br.com.fashionstore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.dao.ClienteDAO;
import br.com.fashionstore.dao.EnderecoDAO;
import br.com.fashionstore.dao.PedidoDAO;
import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.Perfil;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.model.StatusPedido;

/**
 * Teste da área administrativa (Etapa 8) SEM banco: regras de status,
 * consulta de clientes, consulta de pedidos e números do painel.
 *
 * Executar: Ctrl+Shift+P > Tasks: Run Task > "Teste: administração"
 */
public class TesteAdministracao {

    private static int ok = 0;
    private static int falhas = 0;

    // ======================= DAOs falsos =======================

    static class PedidoDAOFalso extends PedidoDAO {
        final List<Pedido> pedidos = new ArrayList<>();
        final List<String> chamadas = new ArrayList<>();

        @Override
        public Pedido buscarPorId(int id) {
            return pedidos.stream().filter(p -> p.getIdPedido() == id).findFirst().orElse(null);
        }

        @Override
        public boolean atualizarStatus(int id, StatusPedido novo) {
            chamadas.add(id + ":" + novo);
            Pedido p = buscarPorId(id);
            if (p == null) {
                return false;
            }
            p.setStatus(novo);
            return true;
        }

        @Override
        public List<Pedido> listarTodos() {
            return pedidos;
        }

        @Override
        public List<Pedido> listarPorStatus(StatusPedido status) {
            return pedidos.stream().filter(p -> p.getStatus() == status).toList();
        }

        @Override
        public List<Pedido> listarPorCliente(int idCliente) {
            return pedidos.stream().filter(p -> p.getCliente().getIdCliente() == idCliente).toList();
        }
    }

    static class ClienteDAOFalso extends ClienteDAO {
        final List<Cliente> clientes = new ArrayList<>();

        @Override
        public List<Cliente> listarTodos() {
            return clientes;
        }
    }

    static class ProdutoDAOFalso extends ProdutoDAO {
        final List<Produto> produtos = new ArrayList<>();

        @Override
        public List<Produto> listarTodos() {
            return produtos;
        }
    }

    // ============================ Testes ============================

    public static void main(String[] args) throws Exception {
        System.out.println("=== Teste da administração - FashionStore ===\n");

        // ---------------- Regras de status ----------------
        System.out.println("-- Caminho de status");
        verificar("PENDENTE -> PAGO permitido", StatusPedido.PENDENTE.podeMudarPara(StatusPedido.PAGO));
        verificar("PENDENTE -> CANCELADO permitido", StatusPedido.PENDENTE.podeMudarPara(StatusPedido.CANCELADO));
        verificar("PENDENTE -> ENVIADO NÃO (pula o pagamento)", !StatusPedido.PENDENTE.podeMudarPara(StatusPedido.ENVIADO));
        verificar("PAGO -> ENVIADO permitido", StatusPedido.PAGO.podeMudarPara(StatusPedido.ENVIADO));
        verificar("PAGO -> PENDENTE NÃO (não volta)", !StatusPedido.PAGO.podeMudarPara(StatusPedido.PENDENTE));
        verificar("ENVIADO -> ENTREGUE permitido", StatusPedido.ENVIADO.podeMudarPara(StatusPedido.ENTREGUE));
        verificar("ENVIADO -> CANCELADO NÃO", !StatusPedido.ENVIADO.podeMudarPara(StatusPedido.CANCELADO));
        verificar("ENTREGUE não muda mais", StatusPedido.ENTREGUE.getProximos().isEmpty());
        verificar("CANCELADO não muda mais", StatusPedido.CANCELADO.getProximos().isEmpty());
        verificar("Próximos de PENDENTE = [PAGO, CANCELADO]",
                StatusPedido.PENDENTE.getProximos().equals(List.of(StatusPedido.PAGO, StatusPedido.CANCELADO)));
        verificar("Texto 'pago' vira PAGO", StatusPedido.de(" pago ") == StatusPedido.PAGO);
        verificar("Texto inválido vira null", StatusPedido.de("XYZ") == null && StatusPedido.de(null) == null);

        // ---------------- Dados de exemplo ----------------
        Cliente ana = cliente(1, "Ana Souza", "ana@email.com", "52998224725", Perfil.CLIENTE);
        Cliente beto = cliente(2, "Beto Lima", "beto@email.com", "11144477735", Perfil.CLIENTE);
        Cliente admin = cliente(3, "Administrador", "admin@loja.com", "00000000000", Perfil.ADMIN);
        Endereco end = new Endereco(1, "01000000", "Rua A", "1", null, "Centro", "São Paulo", "SP");

        PedidoDAOFalso pedidoDAO = new PedidoDAOFalso();
        pedidoDAO.pedidos.add(pedido(1, ana, end, "100.00", StatusPedido.PENDENTE));
        pedidoDAO.pedidos.add(pedido(2, ana, end, "50.00", StatusPedido.PAGO));
        pedidoDAO.pedidos.add(pedido(3, beto, end, "30.00", StatusPedido.CANCELADO));
        pedidoDAO.pedidos.add(pedido(4, beto, end, "20.00", StatusPedido.ENTREGUE));

        ClienteDAOFalso clienteDAO = new ClienteDAOFalso();
        clienteDAO.clientes.addAll(List.of(ana, beto, admin));

        ProdutoDAOFalso produtoDAO = new ProdutoDAOFalso();
        Categoria cat = new Categoria(1, "Camisetas", null);
        produtoDAO.produtos.add(produto(1, "Com estoque", cat, 10));
        produtoDAO.produtos.add(produto(2, "Estoque baixo", cat, 2));
        produtoDAO.produtos.add(produto(3, "Esgotado", cat, 0));

        PedidoService pedidoService = new PedidoService(pedidoDAO, new ProdutoDAO(), new EnderecoDAO());

        // ---------------- Atualizar status ----------------
        System.out.println("-- Atualizar status (admin)");
        verificar("PENDENTE -> PAGO", pedidoService.alterarStatus(1, "PAGO") == StatusPedido.PAGO
                && pedidoDAO.buscarPorId(1).getStatus() == StatusPedido.PAGO);
        verificar("Pular etapa é recusado (PAGO -> ENTREGUE)", campo(() -> pedidoService.alterarStatus(1, "ENTREGUE"), "status"));
        verificar("Pedido entregue não muda", msg(() -> pedidoService.alterarStatus(4, "CANCELADO")).contains("não pode mais ser alterado"));
        verificar("Pedido cancelado não muda", campo(() -> pedidoService.alterarStatus(3, "PAGO"), "status"));
        verificar("Status inválido é recusado", campo(() -> pedidoService.alterarStatus(1, "QUALQUER"), "status"));
        verificar("Pedido inexistente avisa", campo(() -> pedidoService.alterarStatus(99, "PAGO"), "geral"));
        verificar("Sem id avisa", campo(() -> pedidoService.alterarStatus(null, "PAGO"), "geral"));
        pedidoService.alterarStatus(2, "CANCELADO");
        verificar("Cancelar chama o DAO (que devolve o estoque)", pedidoDAO.chamadas.contains("2:CANCELADO"));
        verificar("Recusas não chegaram ao banco", pedidoDAO.chamadas.size() == 2);

        // ---------------- Consulta de pedidos ----------------
        System.out.println("-- Consulta de pedidos");
        verificar("Sem filtro lista todos (4)", pedidoService.listarTodos(null).size() == 4);
        verificar("Filtro CANCELADO lista 2", pedidoService.listarTodos("CANCELADO").size() == 2);
        verificar("Filtro inválido lista todos", pedidoService.listarTodos("abc").size() == 4);
        verificar("Admin vê qualquer pedido", pedidoService.buscar(3) != null);

        // ---------------- Consulta de clientes ----------------
        System.out.println("-- Consulta de clientes");
        ClienteService clienteService = new ClienteService(clienteDAO);
        verificar("Lista sem administradores (2)", clienteService.listarClientes(null).size() == 2);
        verificar("Busca por nome 'ana'", nomes(clienteService.listarClientes("ana")).equals(List.of("Ana Souza")));
        verificar("Busca por e-mail 'beto@'", nomes(clienteService.listarClientes("beto@")).equals(List.of("Beto Lima")));
        verificar("Busca por CPF com pontuação", nomes(clienteService.listarClientes("111.444.777")).equals(List.of("Beto Lima")));
        verificar("Busca que não acha nada", clienteService.listarClientes("zzz").isEmpty());
        verificar("Admin não aparece nem buscando", clienteService.listarClientes("admin").isEmpty());

        // ---------------- Painel ----------------
        System.out.println("-- Painel");
        // Situação agora: 1 PAGO(100), 2 CANCELADO(50), 3 CANCELADO(30), 4 ENTREGUE(20)
        ResumoPainel r = new PainelService(pedidoDAO, clienteDAO, produtoDAO).gerarResumo();
        verificar("Total de clientes = 2 (sem admin)", r.getTotalClientes() == 2);
        verificar("Total de pedidos = 4", r.getTotalPedidos() == 4);
        verificar("Faturamento sem cancelados = 120,00", r.getFaturamento().compareTo(new BigDecimal("120.00")) == 0);
        verificar("2 cancelados, 1 pago, 1 entregue, 0 pendente",
                r.getPedidosPorStatus().get(StatusPedido.CANCELADO) == 2
                && r.getPedidosPorStatus().get(StatusPedido.PAGO) == 1
                && r.getPedidosPorStatus().get(StatusPedido.ENTREGUE) == 1
                && r.getPedidosPorStatus().get(StatusPedido.PENDENTE) == 0);
        verificar("Pedidos em aberto (pendente + pago) = 1", r.getPedidosEmAberto() == 1);
        verificar("Total de produtos = 3", r.getTotalProdutos() == 3);
        verificar("Alerta: 1 esgotado", nomesProdutos(r.getProdutosEsgotados()).equals(List.of("Esgotado")));
        verificar("Alerta: 1 com estoque baixo", nomesProdutos(r.getProdutosEstoqueBaixo()).equals(List.of("Estoque baixo")));
        verificar("Últimos pedidos (até 5)", r.getUltimosPedidos().size() == 4);

        ResumoPainel vazio = new PainelService(new PedidoDAOFalso(), new ClienteDAOFalso(), new ProdutoDAOFalso()).gerarResumo();
        verificar("Loja vazia: tudo zero, sem erro",
                vazio.getTotalPedidos() == 0 && vazio.getFaturamento().signum() == 0 && vazio.getPedidosEmAberto() == 0);

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    // ============================ Auxiliares ============================

    private static Cliente cliente(int id, String nome, String email, String cpf, Perfil perfil) {
        Cliente c = new Cliente(nome, email, null, cpf, null);
        c.setIdCliente(id);
        c.setPerfil(perfil);
        return c;
    }

    private static Pedido pedido(int id, Cliente c, Endereco e, String total, StatusPedido status) {
        Pedido p = new Pedido(c, e);
        p.setIdPedido(id);
        p.setValorTotal(new BigDecimal(total)); // como vem da listagem do banco (sem itens)
        p.setStatus(status);
        return p;
    }

    private static Produto produto(int id, String nome, Categoria cat, int quantidade) {
        Produto p = new Produto(cat, nome, null, new BigDecimal("10"), null);
        p.setIdProduto(id);
        p.adicionarEstoque(new Estoque(id, "M", quantidade));
        return p;
    }

    private static List<String> nomes(List<Cliente> clientes) {
        return clientes.stream().map(Cliente::getNome).toList();
    }

    private static List<String> nomesProdutos(List<Produto> produtos) {
        return produtos.stream().map(Produto::getNome).toList();
    }

    interface Acao {
        void executar() throws ValidacaoException;
    }

    private static boolean campo(Acao acao, String campo) {
        try {
            acao.executar();
            return false;
        } catch (ValidacaoException e) {
            return e.getErros().containsKey(campo);
        }
    }

    private static String msg(Acao acao) {
        try {
            acao.executar();
            return "";
        } catch (ValidacaoException e) {
            return String.join(" ", e.getErros().values());
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
