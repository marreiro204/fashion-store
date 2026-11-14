package br.com.fashionstore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.dao.EnderecoDAO;
import br.com.fashionstore.dao.EstoqueDAO;
import br.com.fashionstore.dao.PedidoDAO;
import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Carrinho;
import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.model.ItemPedido;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.Produto;

/**
 * Teste do carrinho e da finalização de pedidos (Etapa 7) SEM banco,
 * com DAOs "falsos" que simulam produtos, estoque, endereços e pedidos.
 *
 * Executar: Ctrl+Shift+P > Tasks: Run Task > "Teste: carrinho e pedidos"
 */
public class TesteCarrinhoPedidos {

    private static int ok = 0;
    private static int falhas = 0;

    // ======================= "Banco" em memória =======================

    static final Map<Integer, Produto> PRODUTOS = new HashMap<>();

    static class ProdutoDAOFalso extends ProdutoDAO {
        @Override
        public Produto buscarPorId(int id) {
            return PRODUTOS.get(id);
        }
    }

    static class EstoqueDAOFalso extends EstoqueDAO {
        @Override
        public Estoque buscarPorProdutoETamanho(int idProduto, String tamanho) {
            Produto p = PRODUTOS.get(idProduto);
            return p == null ? null : p.getEstoquePorTamanho(tamanho);
        }
    }

    static class EnderecoDAOFalso extends EnderecoDAO {
        final Map<Integer, Endereco> enderecos = new HashMap<>();

        @Override
        public Endereco buscarPorId(int id) {
            return enderecos.get(id);
        }

        @Override
        public List<Endereco> listarPorCliente(int idCliente) {
            return enderecos.values().stream().filter(e -> e.getIdCliente() == idCliente).toList();
        }
    }

    /** Simula a transação: confere tudo, só então baixa o estoque e grava. */
    static class PedidoDAOFalso extends PedidoDAO {
        final List<Pedido> pedidos = new ArrayList<>();
        boolean simularCorrida = false; // outra pessoa comprou a última peça no meio do caminho

        @Override
        public Pedido salvar(Pedido pedido) {
            if (simularCorrida) {
                throw new DAOException(DAOException.Motivo.ESTOQUE_INSUFICIENTE, "Estoque insuficiente");
            }
            for (ItemPedido item : pedido.getItens()) {
                Estoque e = PRODUTOS.get(item.getProduto().getIdProduto()).getEstoquePorTamanho(item.getTamanho());
                if (e == null || !e.temDisponivel(item.getQuantidade())) {
                    throw new DAOException(DAOException.Motivo.ESTOQUE_INSUFICIENTE, "Estoque insuficiente");
                }
            }
            for (ItemPedido item : pedido.getItens()) {
                PRODUTOS.get(item.getProduto().getIdProduto()).getEstoquePorTamanho(item.getTamanho())
                        .baixar(item.getQuantidade());
            }
            pedido.setIdPedido(pedidos.size() + 1);
            pedidos.add(pedido);
            return pedido;
        }

        @Override
        public Pedido buscarPorId(int id) {
            return pedidos.stream().filter(p -> p.getIdPedido() == id).findFirst().orElse(null);
        }

        @Override
        public List<Pedido> listarPorCliente(int idCliente) {
            return pedidos.stream().filter(p -> p.getCliente().getIdCliente() == idCliente).toList();
        }
    }

    // ============================= Testes =============================

    public static void main(String[] args) throws Exception {
        System.out.println("=== Teste do carrinho e pedidos - FashionStore ===\n");

        Categoria cat = new Categoria(1, "Camisetas", null);
        Produto camiseta = produto(1, "Camiseta", "59.90", cat, "P", 5, "M", 2, "G", 0);
        Produto calca = produto(2, "Calça", "149.90", cat, "40", 3);

        ProdutoDAOFalso produtoDAO = new ProdutoDAOFalso();
        EstoqueService estoqueService = new EstoqueService(new EstoqueDAOFalso(), produtoDAO);
        CarrinhoService carrinhoService = new CarrinhoService(produtoDAO, estoqueService);
        EnderecoDAOFalso enderecoDAO = new EnderecoDAOFalso();
        PedidoDAOFalso pedidoDAO = new PedidoDAOFalso();
        PedidoService pedidoService = new PedidoService(pedidoDAO, produtoDAO, enderecoDAO);

        // ---------------- Classe Carrinho ----------------
        System.out.println("-- Classe Carrinho");
        Carrinho c = new Carrinho();
        verificar("Carrinho novo está vazio", c.isVazio() && c.getQuantidadeItens() == 0);
        c.adicionar(camiseta, "P", 1);
        c.adicionar(camiseta, "P", 2);
        verificar("Mesmo produto e tamanho somam (1 + 2 = 3)", c.getItens().size() == 1 && c.getQuantidadeItens() == 3);
        c.adicionar(camiseta, "M", 1);
        verificar("Tamanho diferente vira outro item", c.getItens().size() == 2);
        verificar("Total = 4 x 59,90 = 239,60", igual(c.getTotal(), "239.60"));
        c.alterarQuantidade(1, "M", 0);
        verificar("Quantidade 0 remove o item", c.getItens().size() == 1);
        verificar("Remover item existente", c.remover(1, "p") && c.isVazio());
        verificar("Lista do carrinho é somente leitura",
                lancaExcecao(() -> c.getItens().add(new ItemPedido(camiseta, "P", 1))));

        // ---------------- Adicionar ----------------
        System.out.println("-- Adicionar ao carrinho");
        Carrinho carrinho = new Carrinho();
        carrinhoService.adicionar(carrinho, 1, " p ", "2");
        verificar("Adicionar 2 camisetas P (tamanho normalizado)", carrinho.buscarItem(1, "P").getQuantidade() == 2);
        verificar("Sem tamanho é recusado", campo(() -> carrinhoService.adicionar(carrinho, 1, "", "1"), "tamanho"));
        verificar("Quantidade 0 é recusada", campo(() -> carrinhoService.adicionar(carrinho, 1, "P", "0"), "quantidade"));
        verificar("Quantidade com texto é recusada", campo(() -> carrinhoService.adicionar(carrinho, 1, "P", "abc"), "quantidade"));
        verificar("Tamanho esgotado (G) é recusado", msg(() -> carrinhoService.adicionar(carrinho, 1, "G", "1")).contains("esgotado"));
        verificar("Tamanho inexistente é recusado", campo(() -> carrinhoService.adicionar(carrinho, 1, "XG", "1"), "tamanho"));
        verificar("Produto inexistente é recusado", campo(() -> carrinhoService.adicionar(carrinho, 99, "P", "1"), "geral"));
        verificar("Somando ao carrinho passa do estoque (2 + 4 > 5)",
                msg(() -> carrinhoService.adicionar(carrinho, 1, "P", "4")).contains("Só temos 5"));
        verificar("Tentativas recusadas não mudaram o carrinho", carrinho.getQuantidadeItens() == 2);
        PRODUTOS.get(1).getEstoquePorTamanho("P").setQuantidade(50);
        verificar("Mais de 10 unidades por item é recusado",
                msg(() -> carrinhoService.adicionar(carrinho, 1, "P", "9")).contains("Máximo de 10"));
        PRODUTOS.get(1).getEstoquePorTamanho("P").setQuantidade(5);
        carrinhoService.adicionar(carrinho, 2, "40", "1");
        verificar("Adicionar uma calça 40", carrinho.getItens().size() == 2 && carrinho.getQuantidadeItens() == 3);

        // ---------------- Alterar / remover ----------------
        System.out.println("-- Alterar e remover");
        carrinhoService.alterarQuantidade(carrinho, 1, "P", "3");
        verificar("Alterar camiseta P para 3", carrinho.buscarItem(1, "P").getQuantidade() == 3);
        verificar("Alterar acima do estoque é recusado",
                msg(() -> carrinhoService.alterarQuantidade(carrinho, 2, "40", "4")).contains("Só temos 3"));
        verificar("Alterar item que não está no carrinho avisa",
                campo(() -> carrinhoService.alterarQuantidade(carrinho, 1, "M", "1"), "geral"));
        carrinhoService.adicionar(carrinho, 1, "M", "1");
        carrinhoService.alterarQuantidade(carrinho, 1, "M", "0");
        verificar("Alterar para 0 remove", carrinho.buscarItem(1, "M") == null);
        carrinhoService.adicionar(carrinho, 1, "M", "1");
        carrinhoService.remover(carrinho, 1, "m");
        verificar("Remover item", carrinho.buscarItem(1, "M") == null);

        // ---------------- Atualizar (preço/estoque mudaram) ----------------
        System.out.println("-- Conferência do carrinho");
        verificar("Nada mudou: nenhum aviso", carrinhoService.atualizar(carrinho).isEmpty());
        camiseta.setPreco(new BigDecimal("69.90"));
        List<String> avisos = carrinhoService.atualizar(carrinho);
        verificar("Preço mudou: aviso e novo subtotal (3 x 69,90)",
                avisos.size() == 1 && igual(carrinho.buscarItem(1, "P").getSubtotal(), "209.70"));
        PRODUTOS.get(1).getEstoquePorTamanho("P").setQuantidade(2);
        avisos = carrinhoService.atualizar(carrinho);
        verificar("Estoque diminuiu: quantidade ajustada para 2",
                carrinho.buscarItem(1, "P").getQuantidade() == 2 && avisos.get(0).contains("Só temos 2"));
        PRODUTOS.get(1).getEstoquePorTamanho("P").setQuantidade(5);
        carrinhoService.alterarQuantidade(carrinho, 1, "P", "2");

        Carrinho temp = new Carrinho();
        Produto removido = produto(3, "Boné", "39.90", cat, "U", 1);
        temp.adicionar(removido, "U", 1);
        PRODUTOS.remove(3);
        verificar("Produto excluído sai do carrinho com aviso",
                carrinhoService.atualizar(temp).get(0).contains("não está mais disponível") && temp.isVazio());

        // ---------------- Finalizar pedido ----------------
        System.out.println("-- Finalizar pedido");
        Cliente ana = cliente(1);
        Cliente beto = cliente(2);
        enderecoDAO.enderecos.put(10, endereco(10, 1));
        enderecoDAO.enderecos.put(20, endereco(20, 2));

        verificar("Carrinho vazio não finaliza", campo(() -> pedidoService.finalizar(ana, new Carrinho(), 10), "geral"));
        verificar("Sem endereço não finaliza", campo(() -> pedidoService.finalizar(ana, carrinho, null), "endereco"));
        verificar("Endereço de OUTRO cliente é recusado", campo(() -> pedidoService.finalizar(ana, carrinho, 20), "endereco"));
        verificar("Endereços listados são só da Ana", pedidoService.listarEnderecos(1).size() == 1);

        PRODUTOS.get(2).getEstoquePorTamanho("40").setQuantidade(0);
        verificar("Item esgotado na hora de finalizar: mensagem clara",
                msg(() -> pedidoService.finalizar(ana, carrinho, 10)).contains("esgotou"));
        verificar("Nada foi gravado e o carrinho foi mantido", pedidoDAO.pedidos.isEmpty() && !carrinho.isVazio());
        PRODUTOS.get(2).getEstoquePorTamanho("40").setQuantidade(3);

        pedidoDAO.simularCorrida = true;
        verificar("Outra pessoa comprou antes (corrida): mensagem amigável",
                msg(() -> pedidoService.finalizar(ana, carrinho, 10)).contains("acabou de esgotar"));
        verificar("Carrinho mantido após a corrida", carrinho.getQuantidadeItens() == 3);
        pedidoDAO.simularCorrida = false;

        camiseta.setPreco(new BigDecimal("79.90")); // preço muda de novo antes de finalizar
        Pedido pedido = pedidoService.finalizar(ana, carrinho, 10);
        verificar("Pedido gravado com id", pedido.getIdPedido() == 1);
        verificar("Preço gravado é o ATUAL (79,90), não o do carrinho",
                igual(pedido.getItens().get(0).getPrecoUnitario(), "79.90"));
        verificar("Total = 2 x 79,90 + 1 x 149,90 = 309,70", igual(pedido.getValorTotal(), "309.70"));
        verificar("Estoque baixou (P: 5 -> 3, 40: 3 -> 2)",
                PRODUTOS.get(1).getQuantidadeDisponivel("P") == 3 && PRODUTOS.get(2).getQuantidadeDisponivel("40") == 2);
        verificar("Carrinho esvaziado após a compra", carrinho.isVazio());
        verificar("Pedido nasce PENDENTE", pedido.getStatus().name().equals("PENDENTE"));

        // ---------------- Histórico e privacidade ----------------
        System.out.println("-- Histórico");
        verificar("Histórico da Ana tem 1 pedido", pedidoService.listarHistorico(1).size() == 1);
        verificar("Histórico do Beto está vazio", pedidoService.listarHistorico(2).isEmpty());
        verificar("Ana vê o próprio pedido", pedidoService.buscarDoCliente(1, 1) != null);
        verificar("Beto NÃO vê o pedido da Ana", pedidoService.buscarDoCliente(1, 2) == null);
        verificar("Pedido inexistente = null", pedidoService.buscarDoCliente(99, 1) == null);
        verificar("Data formatada dd/MM/yyyy HH:mm", pedido.getDataFormatada().matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"));

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    // ============================ Auxiliares ============================

    /** Cria um produto com pares tamanho/quantidade e o coloca no "banco". */
    private static Produto produto(int id, String nome, String preco, Categoria cat, Object... grade) {
        Produto p = new Produto(cat, nome, null, new BigDecimal(preco), null);
        p.setIdProduto(id);
        for (int i = 0; i < grade.length; i += 2) {
            p.adicionarEstoque(new Estoque(id, (String) grade[i], (Integer) grade[i + 1]));
        }
        PRODUTOS.put(id, p);
        return p;
    }

    private static Cliente cliente(int id) {
        Cliente c = new Cliente("Cliente " + id, "c" + id + "@t.com", null, "52998224725", null);
        c.setIdCliente(id);
        return c;
    }

    private static Endereco endereco(int id, int idCliente) {
        Endereco e = new Endereco(idCliente, "01000000", "Rua A", "1", null, "Centro", "São Paulo", "SP");
        e.setIdEndereco(id);
        return e;
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

    private static boolean lancaExcecao(Runnable r) {
        try {
            r.run();
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }

    private static boolean igual(BigDecimal v, String esperado) {
        return v != null && v.compareTo(new BigDecimal(esperado)) == 0;
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
