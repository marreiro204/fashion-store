package br.com.fashionstore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.dao.EstoqueDAO;
import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.model.Produto;

/**
 * Teste do controle de estoque por tamanho (Etapa 6) SEM banco de dados,
 * com DAOs "falsos" em memória.
 *
 * Executar: Ctrl+Shift+P > Tasks: Run Task > "Teste: estoque"
 */
public class TesteEstoque {

    private static int ok = 0;
    private static int falhas = 0;

    /** Estoque em memória (simula o UNIQUE produto + tamanho). */
    static class EstoqueDAOEmMemoria extends EstoqueDAO {
        final List<Estoque> lista = new ArrayList<>();

        @Override
        public void definirQuantidade(int idProduto, String tamanho, int quantidade) {
            Estoque e = buscarPorProdutoETamanho(idProduto, tamanho);
            if (e != null) {
                e.setQuantidade(quantidade);
            } else {
                Estoque novo = new Estoque(idProduto, tamanho, quantidade);
                novo.setIdEstoque(lista.size() + 1);
                lista.add(novo);
            }
        }

        @Override
        public Estoque buscarPorProdutoETamanho(int idProduto, String tamanho) {
            return lista.stream()
                    .filter(e -> e.getIdProduto() == idProduto && e.getTamanho().equalsIgnoreCase(tamanho))
                    .findFirst().orElse(null);
        }

        @Override
        public Estoque buscarPorId(int idEstoque) {
            return lista.stream().filter(e -> e.getIdEstoque() == idEstoque).findFirst().orElse(null);
        }

        @Override
        public boolean excluir(int idEstoque) {
            return lista.removeIf(e -> e.getIdEstoque() == idEstoque);
        }
    }

    /** Só o produto 1 existe. */
    static class ProdutoDAOEmMemoria extends ProdutoDAO {
        @Override
        public Produto buscarPorId(int idProduto) {
            if (idProduto != 1) {
                return null;
            }
            Produto p = new Produto(new Categoria(1, "Camisetas", null), "Camiseta", null, new BigDecimal("59.90"), null);
            p.setIdProduto(1);
            return p;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Teste do estoque - FashionStore ===\n");

        EstoqueDAOEmMemoria estoqueDAO = new EstoqueDAOEmMemoria();
        EstoqueService service = new EstoqueService(estoqueDAO, new ProdutoDAOEmMemoria());

        // ---------------- Cadastro de tamanhos ----------------
        System.out.println("-- Adicionar tamanhos");
        service.adicionarTamanho(1, " p ", "10");
        verificar("Adicionar ' p ' grava 'P' com 10", quantidade(estoqueDAO, "P") == 10);
        service.adicionarTamanho(1, "40", "0");
        verificar("Adicionar numeração '40' com 0", quantidade(estoqueDAO, "40") == 0);
        verificar("Tamanho repetido é recusado", erroNoCampo(() -> service.adicionarTamanho(1, "P", "5"), "tamanho"));
        verificar("Tamanho repetido não altera o estoque", quantidade(estoqueDAO, "P") == 10);
        verificar("Tamanho vazio é recusado", erroNoCampo(() -> service.adicionarTamanho(1, "  ", "5"), "tamanho"));
        verificar("Tamanho com espaço é recusado", erroNoCampo(() -> service.adicionarTamanho(1, "G G", "5"), "tamanho"));
        verificar("Tamanho com mais de 10 caracteres é recusado",
                erroNoCampo(() -> service.adicionarTamanho(1, "EXTRAGRANDE", "5"), "tamanho"));
        verificar("Quantidade negativa é recusada", erroNoCampo(() -> service.adicionarTamanho(1, "M", "-1"), "quantidade"));
        verificar("Quantidade com texto é recusada", erroNoCampo(() -> service.adicionarTamanho(1, "M", "dez"), "quantidade"));
        verificar("Quantidade decimal é recusada", erroNoCampo(() -> service.adicionarTamanho(1, "M", "2.5"), "quantidade"));
        verificar("Quantidade acima do limite é recusada",
                erroNoCampo(() -> service.adicionarTamanho(1, "M", "1000000"), "quantidade"));
        verificar("Produto inexistente é recusado", erroNoCampo(() -> service.adicionarTamanho(99, "M", "1"), "geral"));
        verificar("Nada inválido foi gravado", estoqueDAO.lista.size() == 2);

        // ---------------- Alterar quantidade ----------------
        System.out.println("-- Alterar quantidade");
        service.alterarQuantidade(1, "p", "3");
        verificar("Alterar P para 3", quantidade(estoqueDAO, "P") == 3);
        verificar("Alterar tamanho inexistente avisa", erroNoCampo(() -> service.alterarQuantidade(1, "GG", "1"), "tamanho"));
        verificar("Alterar para negativo é recusado", erroNoCampo(() -> service.alterarQuantidade(1, "P", "-5"), "quantidade"));
        verificar("P continua 3 após tentativas inválidas", quantidade(estoqueDAO, "P") == 3);

        // ---------------- Estoque baixo ----------------
        System.out.println("-- Situação");
        verificar("3 unidades = estoque baixo", estoqueDAO.buscarPorProdutoETamanho(1, "P").isEstoqueBaixo());
        verificar("0 unidades NÃO é 'baixo' (é esgotado)", !estoqueDAO.buscarPorProdutoETamanho(1, "40").isEstoqueBaixo());
        service.alterarQuantidade(1, "P", "4");
        verificar("4 unidades não é estoque baixo", !estoqueDAO.buscarPorProdutoETamanho(1, "P").isEstoqueBaixo());

        // ---------------- Disponibilidade (usada pelo carrinho) ----------------
        System.out.println("-- Disponibilidade");
        verificar("Pedir 4 de P (tem 4) é permitido", service.verificarDisponibilidade(1, "P", 4) == 4);
        verificar("Pedir 2 de 'p' (minúsculo) é permitido", service.verificarDisponibilidade(1, "p", 2) == 4);
        verificar("Pedir 5 de P (tem 4) é recusado", mensagem(() -> service.verificarDisponibilidade(1, "P", 5)).contains("Só temos 4"));
        verificar("Tamanho 40 esgotado é recusado", mensagem(() -> service.verificarDisponibilidade(1, "40", 1)).contains("esgotado"));
        verificar("Tamanho inexistente é recusado", erroNoCampo(() -> service.verificarDisponibilidade(1, "XG", 1), "tamanho"));
        verificar("Sem tamanho escolhido é recusado", erroNoCampo(() -> service.verificarDisponibilidade(1, null, 1), "tamanho"));
        verificar("Quantidade zero é recusada", erroNoCampo(() -> service.verificarDisponibilidade(1, "P", 0), "quantidade"));

        // ---------------- Remover ----------------
        System.out.println("-- Remover tamanho");
        int idEstoque40 = estoqueDAO.buscarPorProdutoETamanho(1, "40").getIdEstoque();
        verificar("Remover devolve o id do produto", service.removerTamanho(idEstoque40) == 1);
        verificar("Tamanho 40 foi removido", estoqueDAO.buscarPorProdutoETamanho(1, "40") == null);
        verificar("Remover id inexistente avisa", erroNoCampo(() -> service.removerTamanho(999), "geral"));

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    interface Acao {
        void executar() throws ValidacaoException;
    }

    private static int quantidade(EstoqueDAOEmMemoria dao, String tamanho) {
        Estoque e = dao.buscarPorProdutoETamanho(1, tamanho);
        return e == null ? -1 : e.getQuantidade();
    }

    private static boolean erroNoCampo(Acao acao, String campo) {
        try {
            acao.executar();
            return false;
        } catch (ValidacaoException e) {
            return e.getErros().containsKey(campo);
        }
    }

    /** Mensagem do erro (ou "" se não houve erro). */
    private static String mensagem(Acao acao) {
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
