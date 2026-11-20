package br.com.fashionstore.api;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.fashionstore.dao.CategoriaDAO;
import br.com.fashionstore.dao.ProdutoDAO;
import br.com.fashionstore.model.Carrinho;
import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.model.ItemPedido;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.model.StatusPedido;
import br.com.fashionstore.service.ProdutoService;
import br.com.fashionstore.service.TokenService;
import br.com.fashionstore.util.Json;
import jakarta.servlet.ServletContext;

/**
 * Teste da API do app Flutter (Etapa 10) SEM banco e SEM Tomcat:
 * JSON, tokens, conversão dos objetos e montagem do carrinho do app.
 *
 * Executar: Ctrl+Shift+P > Tasks: Run Task > "Teste: API"
 */
public class TesteApi {

    private static int ok = 0;
    private static int falhas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Teste da API - FashionStore ===\n");

        // ======================= JSON: escrita =======================
        System.out.println("-- JSON: escrita");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nome", "Calça \"Slim\"");
        m.put("preco", new BigDecimal("189.90"));
        m.put("ativo", true);
        m.put("vazio", null);
        m.put("tamanhos", List.of("P", "M"));
        verificar("Objeto simples", Json.escrever(m)
                .equals("{\"nome\":\"Calça \\\"Slim\\\"\",\"preco\":189.90,\"ativo\":true,\"vazio\":null,\"tamanhos\":[\"P\",\"M\"]}"));
        verificar("Quebra de linha e barra escapadas", Json.escrever("a\nb\\c").equals("\"a\\nb\\\\c\""));
        verificar("'<' escapado (seguro dentro de HTML)", Json.escrever("</script>").equals("\"\\u003c/script>\""));
        verificar("Acentos mantidos", Json.escrever("São Paulo").equals("\"São Paulo\""));
        verificar("BigDecimal sem notação científica", Json.escrever(new BigDecimal("1E+3")).equals("1000"));
        verificar("Enum vira texto", Json.escrever(StatusPedido.PAGO).equals("\"PAGO\""));
        verificar("Data no formato ISO", Json.escrever(LocalDateTime.of(2026, 11, 14, 20, 20)).equals("\"2026-11-14T20:20\""));

        // ======================= JSON: leitura =======================
        System.out.println("-- JSON: leitura");
        Map<String, Object> lido = Json.lerObjeto(
                " { \"idEndereco\": 10, \"itens\": [ {\"idProduto\": 1, \"tamanho\": \"M\", \"quantidade\": 2} ],"
                + " \"obs\": \"Cal\\u00e7a \\\"x\\\"\", \"ok\": false, \"nada\": null, \"valor\": -1.5e2 } ");
        verificar("Número inteiro", new BigDecimal("10").equals(lido.get("idEndereco")));
        verificar("Lista de objetos", ((List<?>) lido.get("itens")).size() == 1);
        verificar("Escape \\u00e7 e aspas", "Calça \"x\"".equals(lido.get("obs")));
        verificar("false e null", Boolean.FALSE.equals(lido.get("ok")) && lido.containsKey("nada") && lido.get("nada") == null);
        verificar("Número com expoente", new BigDecimal("-1.5e2").compareTo((BigDecimal) lido.get("valor")) == 0);
        verificar("Ida e volta (escrever -> ler) igual", Json.lerObjeto(Json.escrever(m)).equals(normalizar(m)));
        for (String invalido : new String[]{"{\"a\":1,}", "{a:1}", "{\"a\":1", "{\"a\":1} lixo", "[1,2", "{\"a\":01}",
                "{\"a\":tru}", "{\"a\":\"sem fim}", "", "[" + "[".repeat(40) + "]".repeat(41)}) {
            verificar("JSON inválido recusado: " + (invalido.length() > 20 ? invalido.substring(0, 20) + "..." : invalido),
                    lancaIllegal(() -> Json.ler(invalido)));
        }
        verificar("lerObjeto recusa lista", lancaIllegal(() -> Json.lerObjeto("[1]")));

        // ======================= Tokens =======================
        System.out.println("-- Tokens");
        String t1 = TokenService.gerar(7);
        String t2 = TokenService.gerar(7);
        verificar("Token longo e aleatório (43 caracteres, diferentes)", t1.length() == 43 && !t1.equals(t2));
        verificar("Token válido devolve o cliente 7", Integer.valueOf(7).equals(TokenService.validar(t1)));
        verificar("Token inventado é recusado", TokenService.validar("abc") == null);
        verificar("Token vazio/nulo é recusado", TokenService.validar("") == null && TokenService.validar(null) == null);
        TokenService.revogar(t1);
        verificar("Após logout o token não vale mais", TokenService.validar(t1) == null);
        Method expirar = TokenService.class.getDeclaredMethod("expirarAgora", String.class);
        expirar.setAccessible(true);
        expirar.invoke(null, t2);
        verificar("Token expirado é recusado", TokenService.validar(t2) == null);

        // ======================= Conversor =======================
        System.out.println("-- Conversor (objeto Java -> JSON do app)");
        ServletContext contexto = contextoFalso(Set.of(
                "/assets/images/produtos/feminino/vestido-preto.jpg",
                "/assets/images/produtos/feminino/vestido-preto-2.jpg"));
        ApiConversor conv = new ApiConversor(contexto, "http://10.0.2.2:8080/FashionStore");

        Categoria fem = new Categoria(2, "Feminino", null);
        Produto vestido = new Produto(fem, "Vestido Midi", "Preto", new BigDecimal("249.90"), "feminino/vestido-preto.jpg");
        vestido.setIdProduto(1);
        vestido.adicionarEstoque(new Estoque(1, "P", 5));
        vestido.adicionarEstoque(new Estoque(1, "M", 2));
        Map<String, Object> jp = conv.produto(vestido);
        verificar("Campos do produto com os nomes combinados",
                jp.keySet().equals(Set.of("idProduto", "nome", "descricao", "preco", "categoria", "imagens", "tamanhos", "esgotado")));
        verificar("Imagens com endereço completo (principal + adicional)", jp.get("imagens").equals(List.of(
                "http://10.0.2.2:8080/FashionStore/assets/images/produtos/feminino/vestido-preto.jpg",
                "http://10.0.2.2:8080/FashionStore/assets/images/produtos/feminino/vestido-preto-2.jpg")));
        verificar("Tamanho com quantidade e 'ultimas'", Json.escrever(jp.get("tamanhos"))
                .equals("[{\"tamanho\":\"P\",\"quantidade\":5,\"ultimas\":false},{\"tamanho\":\"M\",\"quantidade\":2,\"ultimas\":true}]"));
        verificar("Preço sai como 249.90", Json.escrever(jp).contains("\"preco\":249.90"));

        Produto semArquivo = new Produto(fem, "X", null, BigDecimal.ONE, "feminino/nao-existe.jpg");
        verificar("Arquivo inexistente: lista de imagens vazia", ((List<?>) conv.produto(semArquivo).get("imagens")).isEmpty());
        Produto externo = new Produto(fem, "Y", null, BigDecimal.ONE, "https://cdn.exemplo.com/y.jpg");
        verificar("Imagem por URL externa é mantida", conv.produto(externo).get("imagens").equals(List.of("https://cdn.exemplo.com/y.jpg")));

        Cliente ana = new Cliente("Ana", "ana@t.com", "$2a$10$hashsecreto", "52998224725", null);
        ana.setIdCliente(3);
        Endereco end = new Endereco(3, "01000000", "Rua A", "1", null, "Centro", "São Paulo", "SP");
        end.setIdEndereco(10);
        ana.adicionarEndereco(end);
        String jsonCliente = Json.escrever(conv.cliente(ana));
        verificar("Cliente NUNCA leva a senha", !jsonCliente.contains("senha") && !jsonCliente.contains("hashsecreto"));
        verificar("Cliente com endereços e idEndereco", jsonCliente.contains("\"idEndereco\":10") && jsonCliente.contains("\"perfil\":\"CLIENTE\""));

        Pedido pedido = new Pedido(ana, end);
        pedido.setIdPedido(12);
        pedido.adicionarItem(new ItemPedido(vestido, "M", 2));
        pedido.setStatus(StatusPedido.PAGO);
        Map<String, Object> jped = conv.pedido(pedido);
        verificar("Pedido com status e descrição", "PAGO".equals(Json.ler(Json.escrever(jped.get("status"))))
                && "Pago".equals(jped.get("statusDescricao")));
        verificar("Pedido com total 499.80 e item com imagem", Json.escrever(jped).contains("\"valorTotal\":499.80")
                && Json.escrever(jped).contains("\"imagem\":\"http://10.0.2.2:8080/FashionStore/assets/images/produtos/feminino/vestido-preto.jpg\""));

        // ======================= Carrinho enviado pelo app =======================
        System.out.println("-- Carrinho do app");
        Map<Integer, Produto> banco = new HashMap<>();
        banco.put(1, vestido);
        ProdutoService produtoService = new ProdutoService(new ProdutoDAO() {
            @Override
            public Produto buscarPorId(int id) {
                return banco.get(id);
            }
        }, new CategoriaDAO());

        List<String> avisos = new ArrayList<>();
        Carrinho c = CarrinhoJson.montar(Json.lerObjeto(
                "{\"itens\":[{\"idProduto\":1,\"tamanho\":\"m\",\"quantidade\":1},{\"idProduto\":1,\"tamanho\":\"M\",\"quantidade\":2}]}"),
                produtoService, true, avisos);
        verificar("Mesmo produto/tamanho soma (1 + 2 = 3, tamanho 'm' -> 'M')",
                c.getItens().size() == 1 && c.buscarItem(1, "M").getQuantidade() == 3);
        verificar("Preço vem do banco, não do app", c.getTotal().compareTo(new BigDecimal("749.70")) == 0);
        Map<String, Object> resumo = CarrinhoJson.resumo(c, conv, avisos);
        verificar("Resumo com total e quantidade", Json.escrever(resumo).contains("\"quantidadeItens\":3,\"total\":749.70"));

        verificar("Estrito: mais de 10 unidades -> 400",
                status(() -> CarrinhoJson.montar(corpo(1, "M", 11), produtoService, true, null)) == 400);
        List<String> av2 = new ArrayList<>();
        Carrinho c2 = CarrinhoJson.montar(corpo(1, "M", 11), produtoService, false, av2);
        verificar("Validação: mais de 10 ajusta para 10 com aviso", c2.buscarItem(1, "M").getQuantidade() == 10 && av2.size() == 1);
        verificar("Estrito: produto inexistente -> 409",
                status(() -> CarrinhoJson.montar(corpo(99, "M", 1), produtoService, true, null)) == 409);
        List<String> av3 = new ArrayList<>();
        verificar("Validação: produto inexistente sai com aviso",
                CarrinhoJson.montar(corpo(99, "M", 1), produtoService, false, av3).isVazio() && av3.size() == 1);
        verificar("Estrito: item sem tamanho -> 400",
                status(() -> CarrinhoJson.montar(Json.lerObjeto("{\"itens\":[{\"idProduto\":1,\"quantidade\":1}]}"), produtoService, true, null)) == 400);
        verificar("Estrito: quantidade 0 -> 400", status(() -> CarrinhoJson.montar(corpo(1, "M", 0), produtoService, true, null)) == 400);
        verificar("Sem lista 'itens' -> 400", status(() -> CarrinhoJson.montar(Json.lerObjeto("{\"itens\":5}"), produtoService, false, new ArrayList<>())) == 400);
        verificar("Quantidade como texto \"2\" é aceita",
                CarrinhoJson.montar(Json.lerObjeto("{\"itens\":[{\"idProduto\":\"1\",\"tamanho\":\"P\",\"quantidade\":\"2\"}]}"),
                        produtoService, true, null).getQuantidadeItens() == 2);

        System.out.println("\n=== Resultado: " + ok + " OK, " + falhas + " falha(s) ===");
        if (falhas > 0) {
            System.exit(1);
        }
    }

    // ============================ Auxiliares ============================

    private static Map<String, Object> corpo(int idProduto, String tamanho, int quantidade) {
        return Json.lerObjeto("{\"itens\":[{\"idProduto\":" + idProduto + ",\"tamanho\":\"" + tamanho + "\",\"quantidade\":" + quantidade + "}]}");
    }

    /** Números lidos do JSON viram BigDecimal: normaliza o mapa original para comparar. */
    private static Object normalizar(Object v) {
        if (v instanceof Map<?, ?> mapa) {
            Map<String, Object> r = new LinkedHashMap<>();
            mapa.forEach((k, x) -> r.put(String.valueOf(k), normalizar(x)));
            return r;
        }
        if (v instanceof List<?> l) {
            return l.stream().map(TesteApi::normalizar).toList();
        }
        return v;
    }

    /** ServletContext "falso": só responde quais arquivos de imagem existem. */
    private static ServletContext contextoFalso(Set<String> existentes) {
        return (ServletContext) Proxy.newProxyInstance(TesteApi.class.getClassLoader(), new Class<?>[]{ServletContext.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("getResource")) {
                        return existentes.contains((String) args[0]) ? new URL("file:/teste" + args[0]) : null;
                    }
                    return null;
                });
    }

    private static int status(Runnable r) {
        try {
            r.run();
            return 0;
        } catch (ApiErro e) {
            return e.getStatus();
        }
    }

    private static boolean lancaIllegal(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
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
