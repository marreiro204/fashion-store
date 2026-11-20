package br.com.fashionstore.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.model.Carrinho;
import br.com.fashionstore.model.ItemPedido;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.service.EstoqueService;
import br.com.fashionstore.service.ProdutoService;

/**
 * Monta um Carrinho a partir da lista de itens enviada pelo app:
 *
 *   "itens": [ { "idProduto": 1, "tamanho": "M", "quantidade": 2 }, ... ]
 *
 * No app, o carrinho fica guardado no aparelho. O servidor recebe só
 * produto + tamanho + quantidade e busca o PREÇO no banco (nunca confia
 * no preço vindo do app).
 */
final class CarrinhoJson {

    private CarrinhoJson() {
    }

    /**
     * @param estrito true  (finalizar pedido): item inválido -> erro 400;
     *                false (validar carrinho): item inválido é ignorado com um aviso.
     * @param avisos  recebe os avisos do modo não estrito
     */
    static Carrinho montar(Map<String, Object> corpo, ProdutoService produtoService,
                           boolean estrito, List<String> avisos) {

        if (!(corpo.get("itens") instanceof List<?> lista)) {
            throw new ApiErro(400, "Envie a lista \"itens\".");
        }
        if (lista.size() > 50) {
            throw new ApiErro(400, "Itens demais no carrinho.");
        }

        Carrinho carrinho = new Carrinho();
        for (Object elemento : lista) {
            if (!(elemento instanceof Map<?, ?> bruto)) {
                throw new ApiErro(400, "Cada item deve ser um objeto {idProduto, tamanho, quantidade}.");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> item = (Map<String, Object>) bruto;

            Integer idProduto = ApiServlet.inteiro(item, "idProduto");
            String tamanho = ApiServlet.texto(item, "tamanho");
            Integer quantidade = ApiServlet.inteiro(item, "quantidade");

            if (idProduto == null || tamanho == null || tamanho.isBlank() || quantidade == null || quantidade < 1) {
                if (estrito) {
                    throw new ApiErro(400, "Cada item precisa de idProduto, tamanho e quantidade (a partir de 1).");
                }
                avisos.add("Um item inválido foi removido do carrinho.");
                continue;
            }

            Produto produto = produtoService.buscar(idProduto);
            if (produto == null) {
                if (estrito) {
                    throw new ApiErro(409, "Um dos produtos não está mais disponível. Atualize o carrinho.");
                }
                avisos.add("Um produto não está mais disponível e foi removido do carrinho.");
                continue;
            }

            String tamanhoNormalizado = EstoqueService.normalizarTamanho(tamanho);
            ItemPedido existente = carrinho.buscarItem(idProduto, tamanhoNormalizado);
            int total = quantidade + (existente == null ? 0 : existente.getQuantidade());
            if (total > Carrinho.MAXIMO_POR_ITEM) {
                if (estrito) {
                    throw new ApiErro(400, "Máximo de " + Carrinho.MAXIMO_POR_ITEM + " unidades por produto e tamanho.");
                }
                quantidade = Carrinho.MAXIMO_POR_ITEM - (existente == null ? 0 : existente.getQuantidade());
                avisos.add("Máximo de " + Carrinho.MAXIMO_POR_ITEM + " unidades de " + produto.getNome() + ". A quantidade foi ajustada.");
                if (quantidade <= 0) {
                    continue;
                }
            }
            carrinho.adicionar(produto, tamanhoNormalizado, quantidade);
        }
        return carrinho;
    }

    /** Resumo do carrinho para devolver ao app. */
    static Map<String, Object> resumo(Carrinho carrinho, ApiConversor conversor, List<String> avisos) {
        List<Map<String, Object>> itens = new ArrayList<>();
        for (ItemPedido item : carrinho.getItens()) {
            itens.add(conversor.item(item));
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("itens", itens);
        m.put("quantidadeItens", carrinho.getQuantidadeItens());
        m.put("total", carrinho.getTotal().setScale(2, java.math.RoundingMode.HALF_UP));
        m.put("avisos", avisos);
        return m;
    }
}
