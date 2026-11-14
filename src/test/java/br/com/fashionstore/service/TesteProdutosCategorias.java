package br.com.fashionstore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.fashionstore.dao.CategoriaDAO;
import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.util.Validador;

/**
 * Teste das regras de produtos e categorias (Etapa 5) SEM banco de dados,
 * usando DAOs "falsos" em memória (mesma ideia do TesteAutenticacao).
 *
 * Executar: Ctrl+Shift+P > Tasks: Run Task > "Teste: produtos e categorias"
 */
public class TesteProdutosCategorias {

    private static int ok = 0;
    private static int falhas = 0;

    /** Categorias em memória; simula o UNIQUE do nome e o RESTRICT da exclusão. */
    static class CategoriaDAOEmMemoria extends CategoriaDAO {
        final List<Categoria> lista = new ArrayList<>();
        final List<Integer> comProdutos = new ArrayList<>();

        @Override
        public Categoria inserir(Categoria c) {
            if (lista.stream().anyMatch(x -> x.getNome().equalsIgnoreCase(c.getNome()))) {
                throw new DAOException(DAOException.Motivo.DUPLICADO, "Duplicate entry");
            }
            c.setIdCategoria(lista.size() + 1);
            lista.add(c);
            return c;
        }

        @Override
        public boolean atualizar(Categoria c) {
            return lista.stream().anyMatch(x -> x.getIdCategoria() == c.getIdCategoria());
        }

        @Override
        public boolean excluir(int id) {
            if (comProdutos.contains(id)) {
                throw new DAOException(DAOException.Motivo.EM_USO, "Cannot delete a parent row");
            }
            return lista.removeIf(x -> x.getIdCategoria() == id);
        }

        @Override
        public Categoria buscarPorId(int id) {
            return lista.stream().filter(x -> x.getIdCategoria() == id).findFirst().orElse(null);
        }

        @Override
        public List<Categoria> listarTodas() {
            return lista;
        }
    }

    /** Produtos em memória; simula o RESTRICT de produto já vendido. */
    static class ProdutoDAOEmMemoria extends ProdutoDAO {
        final List<Produto> lista = new ArrayList<>();
        final List<Integer> vendidos = new ArrayList<>();
        String ultimaBusca;
        Integer ultimaCategoria;

        @Override
        public Produto inserir(Produto p) {
            p.setIdProduto(lista.size() + 1);
            lista.add(p);
            return p;
        }

        @Override
        public boolean atualizar(Produto p) {
            return lista.stream().anyMatch(x -> x.getIdProduto() == p.getIdProduto());
        }

        @Override
        public boolean excluir(int id) {
            if (vendidos.contains(id)) {
                throw new DAOException(DAOException.Motivo.EM_USO, "Cannot delete a parent row");
            }
            return lista.removeIf(x -> x.getIdProduto() == id);
        }

        @Override
        public List<Produto> buscar(String texto, Integer idCategoria) {
            ultimaBusca = texto;
            ultimaCategoria = idCategoria;
            return lista;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Teste de produtos e categorias - FashionStore ===\n");

        // ---------------- Conversões do Validador ----------------
        System.out.println("-- Preço e imagem");
        verificar("'59,90' vira 59.90", igual(Validador.converterPreco("59,90"), "59.90"));
        verificar("'1.299,90' vira 1299.90", igual(Validador.converterPreco("1.299,90"), "1299.90"));
        verificar("'R$ 59.9' vira 59.90", igual(Validador.converterPreco("R$ 59.9"), "59.90"));
        verificar("'150' vira 150.00", igual(Validador.converterPreco("150"), "150.00"));
        verificar("'abc' é inválido", Validador.converterPreco("abc") == null);
        verificar("'-10' é inválido", Validador.converterPreco("-10") == null);
        verificar("'10,999' (3 casas) é inválido", Validador.converterPreco("10,999") == null);
        verificar("Imagem 'camiseta-azul.jpg' é aceita", Validador.imagemValida("camiseta-azul.jpg"));
        verificar("Imagem 'https://site.com/a.png' é aceita", Validador.imagemValida("https://site.com/a.png"));
        verificar("Imagem 'javascript:alert(1)' é recusada", !Validador.imagemValida("javascript:alert(1)"));
        verificar("Imagem '../../web.xml' é recusada", !Validador.imagemValida("../../web.xml"));
        verificar("Imagem 'arquivo.exe' é recusada", !Validador.imagemValida("arquivo.exe"));
        verificar("Id 'abc' vira null (sem erro)", Validador.converterInteiro("abc") == null);

        // ---------------- Categorias ----------------
        System.out.println("-- Categorias");
        CategoriaDAOEmMemoria categoriaDAO = new CategoriaDAOEmMemoria();
        CategoriaService categorias = new CategoriaService(categoriaDAO);

        Categoria camisetas = categorias.salvar(new Categoria("  Camisetas ", ""));
        verificar("Cadastrar categoria (nome sem espaços extras)",
                camisetas.getIdCategoria() == 1 && "Camisetas".equals(camisetas.getNome()));
        verificar("Descrição vazia vira null", camisetas.getDescricao() == null);
        verificar("Nome repetido é recusado", erroNoCampo(() -> categorias.salvar(new Categoria("camisetas", null)), "nome"));
        verificar("Nome com 1 letra é recusado", erroNoCampo(() -> categorias.salvar(new Categoria("A", null)), "nome"));
        verificar("Alterar categoria inexistente avisa", erroNoCampo(() -> categorias.salvar(new Categoria(99, "Bonés", null)), "geral"));

        categoriaDAO.comProdutos.add(camisetas.getIdCategoria());
        verificar("Excluir categoria com produtos é recusado (mensagem amigável)",
                erroNoCampo(() -> categorias.excluir(camisetas.getIdCategoria()), "geral"));
        Categoria vazia = categorias.salvar(new Categoria("Bonés", null));
        categorias.excluir(vazia.getIdCategoria());
        verificar("Excluir categoria sem produtos funciona", categoriaDAO.buscarPorId(vazia.getIdCategoria()) == null);

        // ---------------- Produtos ----------------
        System.out.println("-- Produtos");
        ProdutoDAOEmMemoria produtoDAO = new ProdutoDAOEmMemoria();
        ProdutoService produtos = new ProdutoService(produtoDAO, categoriaDAO);

        Produto camiseta = new Produto(null, " Camiseta Básica ", "Algodão", null, "camiseta.jpg");
        produtos.salvar(camiseta, "59,90", camisetas.getIdCategoria());
        verificar("Cadastrar produto válido", camiseta.getIdProduto() == 1);
        verificar("Preço convertido para 59.90", igual(camiseta.getPreco(), "59.90"));
        verificar("Categoria ligada ao produto", camiseta.getCategoria() == camisetas);
        verificar("Nome sem espaços extras", "Camiseta Básica".equals(camiseta.getNome()));

        verificar("Preço zero é recusado", erroNoCampo(() ->
                produtos.salvar(new Produto(null, "Calça", null, null, null), "0", 1), "preco"));
        verificar("Preço com texto é recusado", erroNoCampo(() ->
                produtos.salvar(new Produto(null, "Calça", null, null, null), "caro", 1), "preco"));
        verificar("Categoria inexistente é recusada", erroNoCampo(() ->
                produtos.salvar(new Produto(null, "Calça", null, null, null), "10", 999), "categoria"));
        verificar("Sem categoria é recusado", erroNoCampo(() ->
                produtos.salvar(new Produto(null, "Calça", null, null, null), "10", null), "categoria"));
        verificar("Imagem perigosa é recusada", erroNoCampo(() ->
                produtos.salvar(new Produto(null, "Calça", null, null, "javascript:alert(1)"), "10", 1), "imagem"));
        verificar("Nome curto é recusado", erroNoCampo(() ->
                produtos.salvar(new Produto(null, "AB", null, null, null), "10", 1), "nome"));
        verificar("Nenhum produto inválido foi gravado", produtoDAO.lista.size() == 1);

        camiseta.setNome("Camiseta Premium");
        produtos.salvar(camiseta, "79.90", camisetas.getIdCategoria());
        verificar("Alterar produto (nome e preço)", "Camiseta Premium".equals(camiseta.getNome())
                && igual(camiseta.getPreco(), "79.90") && produtoDAO.lista.size() == 1);

        produtos.listarCatalogo("  jeans  ", 2);
        verificar("Pesquisa repassa texto limpo e categoria ao DAO",
                "jeans".equals(produtoDAO.ultimaBusca) && Integer.valueOf(2).equals(produtoDAO.ultimaCategoria));
        produtos.listarCatalogo("x".repeat(500), null);
        verificar("Pesquisa muito longa é cortada em 100 caracteres", produtoDAO.ultimaBusca.length() == 100);

        produtoDAO.vendidos.add(camiseta.getIdProduto());
        verificar("Excluir produto já vendido é recusado (mensagem amigável)",
                erroNoCampo(() -> produtos.excluir(camiseta.getIdProduto()), "geral"));
        produtoDAO.vendidos.clear();
        produtos.excluir(camiseta.getIdProduto());
        verificar("Excluir produto não vendido funciona", produtoDAO.lista.isEmpty());
        verificar("Excluir produto inexistente avisa", erroNoCampo(() -> produtos.excluir(42), "geral"));

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    interface Acao {
        void executar() throws ValidacaoException;
    }

    /** Executa e confere se deu ValidacaoException no campo esperado. */
    private static boolean erroNoCampo(Acao acao, String campo) {
        try {
            acao.executar();
            return false;
        } catch (ValidacaoException e) {
            return e.getErros().containsKey(campo);
        }
    }

    private static boolean igual(BigDecimal valor, String esperado) {
        return valor != null && valor.compareTo(new BigDecimal(esperado)) == 0;
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
