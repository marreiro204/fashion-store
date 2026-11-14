package br.com.fashionstore.model;

import java.math.BigDecimal;

/**
 * Teste manual das classes Model (Etapa 2).
 *
 * Fica em src/test/java, então NÃO vai para o WAR.
 * Execute pelo VS Code clicando em "Run" acima do método main.
 *
 * Cada verificação imprime [OK] ou [FALHOU]. No final, mostra o total.
 */
public class TesteModelos {

    private static int ok = 0;
    private static int falhas = 0;

    public static void main(String[] args) {

        System.out.println("=== Teste das classes Model - FashionStore ===\n");

        // ---------- Categoria e Produto ----------
        Categoria camisetas = new Categoria(1, "Camisetas", "Camisetas masculinas e femininas");

        Produto camiseta = new Produto(camisetas, "Camiseta Básica",
                "Camiseta 100% algodão", new BigDecimal("59.90"), "camiseta-basica.jpg");
        camiseta.setIdProduto(10);

        // ---------- Estoque por tamanho (Proposta 1) ----------
        camiseta.adicionarEstoque(new Estoque(0, "P", 5));
        camiseta.adicionarEstoque(new Estoque(0, "M", 3));
        camiseta.adicionarEstoque(new Estoque(0, "G", 0));

        verificar("Produto pertence à categoria Camisetas",
                camiseta.getCategoria().getNome().equals("Camisetas"));
        verificar("Estoque ligado ao produto 10",
                camiseta.getEstoquePorTamanho("P").getIdProduto() == 10);
        verificar("Quantidade do tamanho M = 3", camiseta.getQuantidadeDisponivel("M") == 3);
        verificar("Tamanho inexistente (GG) = 0", camiseta.getQuantidadeDisponivel("GG") == 0);
        verificar("Quantidade total = 8", camiseta.getQuantidadeTotal() == 8);
        verificar("Tamanhos disponíveis = [P, M] (G está zerado)",
                camiseta.getTamanhosDisponiveis().toString().equals("[P, M]"));
        verificar("Produto não está esgotado", !camiseta.isEsgotado());

        // ---------- Regras do Estoque ----------
        Estoque estoqueP = camiseta.getEstoquePorTamanho("P");
        estoqueP.baixar(2);
        verificar("Baixar 2 do P deixa 3", estoqueP.getQuantidade() == 3);
        verificar("Não permite baixar mais do que existe",
                lancaExcecao(() -> estoqueP.baixar(10)));
        verificar("Não permite quantidade negativa",
                lancaExcecao(() -> estoqueP.setQuantidade(-1)));
        estoqueP.repor(4);
        verificar("Repor 4 deixa 7", estoqueP.getQuantidade() == 7);

        // ---------- Cliente, perfil e endereço (Proposta 2) ----------
        Cliente cliente = new Cliente("Ana Souza", "ana@email.com", "<hash>", "12345678900", "11999990000");
        cliente.setIdCliente(1);
        verificar("Cliente novo tem perfil CLIENTE", cliente.getPerfil() == Perfil.CLIENTE);
        verificar("Cliente comum não é admin", !cliente.isAdmin());

        Cliente admin = new Cliente();
        admin.setPerfil(Perfil.ADMIN);
        verificar("Perfil ADMIN é admin", admin.isAdmin());

        Endereco endereco = new Endereco(0, "01000-000", "Rua das Flores", "100",
                "Apto 2", "Centro", "São Paulo", "SP");
        cliente.adicionarEndereco(endereco);
        verificar("Endereço ligado ao cliente 1", endereco.getIdCliente() == 1);
        verificar("Cliente tem 1 endereço", cliente.getEnderecos().size() == 1);
        verificar("toString do cliente não mostra a senha", !cliente.toString().contains("<hash>"));

        // ---------- Pedido e itens ----------
        Pedido pedido = new Pedido(cliente, endereco);
        verificar("Pedido nasce PENDENTE", pedido.getStatus() == StatusPedido.PENDENTE);

        ItemPedido item1 = new ItemPedido(camiseta, "M", 2);   // 2 x 59,90 = 119,80
        verificar("Item copia o preço do produto (59.90)",
                item1.getPrecoUnitario().compareTo(new BigDecimal("59.90")) == 0);
        verificar("Subtotal 2 x 59.90 = 119.80",
                item1.getSubtotal().compareTo(new BigDecimal("119.80")) == 0);

        Produto calca = new Produto(new Categoria(2, "Calças", ""), "Calça Jeans",
                "Jeans azul", new BigDecimal("149.90"), "calca.jpg");
        ItemPedido item2 = new ItemPedido(calca, "40", 1);    // 149,90

        pedido.adicionarItem(item1);
        pedido.adicionarItem(item2);
        verificar("Valor total = 269.70",
                pedido.getValorTotal().compareTo(new BigDecimal("269.70")) == 0);
        verificar("Quantidade de peças = 3", pedido.getQuantidadeItens() == 3);

        item1.setQuantidade(3); // muda a quantidade -> subtotal recalculado
        verificar("Alterar quantidade recalcula subtotal (179.70)",
                item1.getSubtotal().compareTo(new BigDecimal("179.70")) == 0);
        verificar("Total do pedido acompanha a mudança (329.60)",
                pedido.getValorTotal().compareTo(new BigDecimal("329.60")) == 0);

        Pedido pedidoDoBanco = new Pedido(); // simula listagem: sem itens carregados
        pedidoDoBanco.setValorTotal(new BigDecimal("99.90"));
        verificar("Pedido sem itens carregados usa o total gravado (99.90)",
                pedidoDoBanco.getValorTotal().compareTo(new BigDecimal("99.90")) == 0);

        camiseta.setPreco(new BigDecimal("79.90")); // preço do produto muda depois
        verificar("Preço do item não muda se o produto mudar de preço",
                item1.getPrecoUnitario().compareTo(new BigDecimal("59.90")) == 0);

        verificar("Quantidade zero no item não é permitida",
                lancaExcecao(() -> item2.setQuantidade(0)));

        // ---------- Status do pedido ----------
        pedido.alterarStatus(StatusPedido.PAGO);
        pedido.alterarStatus(StatusPedido.ENVIADO);
        pedido.alterarStatus(StatusPedido.ENTREGUE);
        verificar("Status chegou em ENTREGUE", pedido.getStatus() == StatusPedido.ENTREGUE);
        verificar("Pedido entregue não pode mudar de status",
                lancaExcecao(() -> pedido.alterarStatus(StatusPedido.CANCELADO)));
        verificar("Descrição amigável do status", pedido.getStatus().getDescricao().equals("Entregue"));

        // ---------- Exibição ----------
        System.out.println("\nExemplos de toString():");
        System.out.println("  " + camiseta);
        System.out.println("  " + cliente);
        System.out.println("  " + endereco.getEnderecoFormatado());
        System.out.println("  " + pedido);

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

    /** Retorna true se o código lançar uma exceção (o que esperamos em regras violadas). */
    private static boolean lancaExcecao(Runnable codigo) {
        try {
            codigo.run();
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }
}
