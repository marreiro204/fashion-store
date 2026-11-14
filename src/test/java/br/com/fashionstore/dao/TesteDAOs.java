package br.com.fashionstore.dao;

import java.math.BigDecimal;
import java.util.List;

import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.model.ItemPedido;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.model.StatusPedido;

/**
 * Teste dos DAOs em um banco REAL (Etapa 3).
 *
 * Requer: banco fashionstore criado + db.properties preenchido.
 *
 * Segue o roteiro de demonstração do PDF:
 *   Cadastrar -> Consultar -> Alterar -> Consultar novamente -> Excluir
 * e também testa as transações do pedido (baixa e devolução de estoque).
 *
 * Os dados criados aqui usam nomes "TESTE-DAO-..." e são APAGADOS no
 * final, mesmo se algum passo falhar. O banco volta como estava.
 *
 * Executar: Ctrl+Shift+P > Tasks: Run Task > "Teste: DAOs (CRUD)"
 */
public class TesteDAOs {

    private static int ok = 0;
    private static int falhas = 0;

    private static final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private static final ProdutoDAO produtoDAO = new ProdutoDAO();
    private static final EstoqueDAO estoqueDAO = new EstoqueDAO();
    private static final ClienteDAO clienteDAO = new ClienteDAO();
    private static final EnderecoDAO enderecoDAO = new EnderecoDAO();
    private static final PedidoDAO pedidoDAO = new PedidoDAO();
    private static final ItemPedidoDAO itemPedidoDAO = new ItemPedidoDAO();

    public static void main(String[] args) {
        System.out.println("=== Teste dos DAOs - FashionStore ===\n");

        String sufixo = String.valueOf(System.currentTimeMillis() % 100000000L);
        Categoria categoria = null;
        Produto produto = null;
        Cliente cliente = null;
        Pedido pedido = null;

        try {
            // ---------------- CATEGORIA: CRUD ----------------
            System.out.println("-- Categoria");
            categoria = categoriaDAO.inserir(new Categoria("TESTE-DAO-" + sufixo, "Categoria de teste"));
            verificar("Cadastrar: id gerado pelo banco", categoria.getIdCategoria() > 0);

            Categoria lida = categoriaDAO.buscarPorId(categoria.getIdCategoria());
            verificar("Consultar: encontrou pelo id", lida != null && lida.getNome().equals(categoria.getNome()));

            categoria.setDescricao("Descrição alterada");
            verificar("Alterar: atualizou", categoriaDAO.atualizar(categoria));
            verificar("Consultar novamente: descrição nova",
                    "Descrição alterada".equals(categoriaDAO.buscarPorId(categoria.getIdCategoria()).getDescricao()));

            final String nomeCategoria = categoria.getNome();
            verificar("Nome repetido é recusado (DUPLICADO)",
                    motivo(() -> categoriaDAO.inserir(new Categoria(nomeCategoria, null))) == DAOException.Motivo.DUPLICADO);

            // ---------------- PRODUTO + ESTOQUE ----------------
            System.out.println("-- Produto e estoque");
            produto = new Produto(categoria, "TESTE-DAO Camiseta " + sufixo, "Produto de teste",
                    new BigDecimal("59.90"), "teste.jpg");
            produto.adicionarEstoque(new Estoque(0, "P", 5));
            produto.adicionarEstoque(new Estoque(0, "M", 2));
            produtoDAO.inserir(produto);
            verificar("Cadastrar produto com 2 tamanhos (transação)", produto.getIdProduto() > 0);

            Produto produtoLido = produtoDAO.buscarPorId(produto.getIdProduto());
            verificar("Consultar: categoria veio junto (JOIN)",
                    produtoLido.getCategoria().getNome().equals(categoria.getNome()));
            verificar("Consultar: estoque P = 5 e M = 2",
                    produtoLido.getQuantidadeDisponivel("P") == 5 && produtoLido.getQuantidadeDisponivel("M") == 2);

            produto.setPreco(new BigDecimal("69.90"));
            produtoDAO.atualizar(produto);
            verificar("Alterar preço para 69.90",
                    produtoDAO.buscarPorId(produto.getIdProduto()).getPreco().compareTo(new BigDecimal("69.90")) == 0);

            List<Produto> pesquisa = produtoDAO.buscar("TESTE-DAO Camiseta " + sufixo, categoria.getIdCategoria());
            verificar("Pesquisa por texto + categoria encontra 1", pesquisa.size() == 1);
            verificar("Pesquisa com '%' não quebra (escape do LIKE)", produtoDAO.buscar("100%_x", null) != null);

            estoqueDAO.definirQuantidade(produto.getIdProduto(), "G", 3); // tamanho novo
            verificar("Definir quantidade cria tamanho G = 3",
                    estoqueDAO.buscarPorProdutoETamanho(produto.getIdProduto(), "G").getQuantidade() == 3);
            final int idProduto = produto.getIdProduto();
            verificar("Tamanho repetido é recusado (DUPLICADO)",
                    motivo(() -> estoqueDAO.inserir(new Estoque(idProduto, "P", 1))) == DAOException.Motivo.DUPLICADO);

            // ---------------- CLIENTE + ENDEREÇO ----------------
            System.out.println("-- Cliente e endereço");
            cliente = new Cliente("TESTE-DAO Cliente", "teste-dao-" + sufixo + "@teste.com",
                    "$2a$10$hashDeTesteNaoUsadoParaLogin000000000000000000000000",
                    "9" + String.format("%010d", Long.parseLong(sufixo)), "11999999999");
            cliente.adicionarEndereco(new Endereco(0, "01000000", "Rua Teste", "1", null,
                    "Centro", "São Paulo", "SP"));
            clienteDAO.inserir(cliente);
            verificar("Cadastrar cliente + endereço (transação)",
                    cliente.getIdCliente() > 0 && cliente.getEnderecos().get(0).getIdEndereco() > 0);
            verificar("Buscar por e-mail (login)", clienteDAO.buscarPorEmail(cliente.getEmail()) != null);
            verificar("existeEmail = true", clienteDAO.existeEmail(cliente.getEmail()));
            verificar("Cliente veio com 1 endereço",
                    clienteDAO.buscarPorId(cliente.getIdCliente()).getEnderecos().size() == 1);
            verificar("Endereços listados por cliente",
                    enderecoDAO.listarPorCliente(cliente.getIdCliente()).size() == 1);

            // ---------------- PEDIDO (TRANSAÇÕES) ----------------
            System.out.println("-- Pedido");
            Endereco endereco = cliente.getEnderecos().get(0);
            Produto produtoAtual = produtoDAO.buscarPorId(produto.getIdProduto());

            pedido = new Pedido(cliente, endereco);
            pedido.adicionarItem(new ItemPedido(produtoAtual, "M", 2)); // usa as 2 unidades de M
            pedidoDAO.salvar(pedido);
            verificar("Salvar pedido (transação)", pedido.getIdPedido() > 0);
            verificar("Estoque M baixou para 0",
                    estoqueDAO.buscarPorProdutoETamanho(produto.getIdProduto(), "M").getQuantidade() == 0);

            Pedido pedidoLido = pedidoDAO.buscarPorId(pedido.getIdPedido());
            verificar("Consultar pedido com 1 item e total 139.80",
                    pedidoLido.getItens().size() == 1
                    && pedidoLido.getValorTotal().compareTo(new BigDecimal("139.80")) == 0);
            verificar("Item guardou o tamanho M", "M".equals(itemPedidoDAO.listarPorPedido(pedido.getIdPedido()).get(0).getTamanho()));
            verificar("Histórico do cliente tem 1 pedido",
                    pedidoDAO.listarPorCliente(cliente.getIdCliente()).size() == 1);

            // Pedido sem estoque: P tem 5, pedimos 1 de P e 1 de M (M = 0) -> deve desfazer TUDO
            Pedido semEstoque = new Pedido(cliente, endereco);
            semEstoque.adicionarItem(new ItemPedido(produtoAtual, "P", 1));
            semEstoque.adicionarItem(new ItemPedido(produtoAtual, "M", 1));
            verificar("Pedido sem estoque é recusado (ESTOQUE_INSUFICIENTE)",
                    motivo(() -> pedidoDAO.salvar(semEstoque)) == DAOException.Motivo.ESTOQUE_INSUFICIENTE);
            verificar("Rollback: estoque P continua 5",
                    estoqueDAO.buscarPorProdutoETamanho(produto.getIdProduto(), "P").getQuantidade() == 5);
            verificar("Rollback: nenhum pedido extra criado",
                    pedidoDAO.listarPorCliente(cliente.getIdCliente()).size() == 1);

            pedidoDAO.atualizarStatus(pedido.getIdPedido(), StatusPedido.PAGO);
            verificar("Status alterado para PAGO",
                    pedidoDAO.buscarPorId(pedido.getIdPedido()).getStatus() == StatusPedido.PAGO);

            pedidoDAO.atualizarStatus(pedido.getIdPedido(), StatusPedido.CANCELADO);
            verificar("Cancelar devolve o estoque (M volta a 2)",
                    estoqueDAO.buscarPorProdutoETamanho(produto.getIdProduto(), "M").getQuantidade() == 2);
            pedidoDAO.atualizarStatus(pedido.getIdPedido(), StatusPedido.CANCELADO);
            verificar("Cancelar de novo NÃO devolve duas vezes (M continua 2)",
                    estoqueDAO.buscarPorProdutoETamanho(produto.getIdProduto(), "M").getQuantidade() == 2);

            // ---------------- REGRAS DE EXCLUSÃO ----------------
            System.out.println("-- Exclusões");
            final int idProd = produto.getIdProduto();
            verificar("Produto vendido não pode ser excluído (EM_USO)",
                    motivo(() -> produtoDAO.excluir(idProd)) == DAOException.Motivo.EM_USO);
            final int idCat = categoria.getIdCategoria();
            verificar("Categoria com produto não pode ser excluída (EM_USO)",
                    motivo(() -> categoriaDAO.excluir(idCat)) == DAOException.Motivo.EM_USO);

        } catch (RuntimeException e) {
            falhas++;
            System.out.println("[ERRO INESPERADO] " + e.getMessage());
            e.printStackTrace(System.out);

        } finally {
            // ---------------- LIMPEZA (sempre executa) ----------------
            System.out.println("-- Limpeza dos dados de teste");
            try {
                if (pedido != null && pedido.getIdPedido() > 0) {
                    verificar("Excluir pedido de teste", pedidoDAO.excluir(pedido.getIdPedido()));
                }
                if (cliente != null && cliente.getIdCliente() > 0) {
                    verificar("Excluir cliente de teste (endereço vai junto)", clienteDAO.excluir(cliente.getIdCliente()));
                }
                if (produto != null && produto.getIdProduto() > 0) {
                    verificar("Excluir produto de teste (estoque vai junto)", produtoDAO.excluir(produto.getIdProduto()));
                }
                if (categoria != null && categoria.getIdCategoria() > 0) {
                    verificar("Excluir categoria de teste", categoriaDAO.excluir(categoria.getIdCategoria()));
                    verificar("Consultar após excluir: não existe mais",
                            categoriaDAO.buscarPorId(categoria.getIdCategoria()) == null);
                }
            } catch (RuntimeException e) {
                falhas++;
                System.out.println("[ERRO NA LIMPEZA] " + e.getMessage()
                        + " -> apague manualmente os registros com 'TESTE-DAO' no nome.");
            }
        }

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        if (falhas > 0) {
            System.exit(1);
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

    /** Executa o código e devolve o motivo da DAOException lançada (ou null se não lançou). */
    private static DAOException.Motivo motivo(Runnable codigo) {
        try {
            codigo.run();
            return null;
        } catch (DAOException e) {
            return e.getMotivo();
        }
    }
}
