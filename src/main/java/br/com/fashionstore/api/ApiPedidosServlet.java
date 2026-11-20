package br.com.fashionstore.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.model.Carrinho;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.service.CarrinhoService;
import br.com.fashionstore.service.PedidoService;
import br.com.fashionstore.service.ProdutoService;
import br.com.fashionstore.service.ValidacaoException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Carrinho e pedidos do cliente.
 *
 *   POST /api/carrinho/validar  -> confere preços e estoque do carrinho do app (público)
 *   POST /api/pedidos           -> finaliza a compra (login)
 *   GET  /api/pedidos           -> histórico (login)
 *   GET  /api/pedidos/12        -> detalhes, só se o pedido for do cliente (login)
 *
 * A finalização usa o MESMO PedidoService do site: preço atual do banco,
 * conferência de estoque e gravação em transação com baixa no estoque.
 */
@WebServlet(urlPatterns = {"/api/carrinho/validar", "/api/pedidos/*"})   // "/api/pedidos/*" também atende "/api/pedidos"
public class ApiPedidosServlet extends ApiServlet {

    private static final long serialVersionUID = 1L;

    private final ProdutoService produtoService = new ProdutoService();
    private final CarrinhoService carrinhoService = new CarrinhoService();
    private final PedidoService pedidoService = new PedidoService();

    @Override
    protected Object get(HttpServletRequest request, HttpServletResponse response) {
        if (!request.getServletPath().equals("/api/pedidos")) {
            throw new ApiErro(405, "Método não permitido.");
        }
        Cliente cliente = exigirCliente(request);
        ApiConversor conversor = new ApiConversor(request);

        Integer id = idDaUrl(request);
        if (id != null) {
            Pedido pedido = pedidoService.buscarDoCliente(id, cliente.getIdCliente());
            if (pedido == null) {
                // Não existe ou é de outro cliente: mesma resposta (não revela qual).
                throw new ApiErro(404, "Pedido não encontrado.");
            }
            return conversor.pedido(pedido);
        }

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Pedido p : pedidoService.listarHistorico(cliente.getIdCliente())) {
            lista.add(conversor.pedido(p));
        }
        return lista;
    }

    @Override
    protected Object post(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("/api/carrinho/validar".equals(request.getServletPath())) {
            return validarCarrinho(request);
        }
        if (request.getPathInfo() != null && !request.getPathInfo().equals("/")) {
            throw new ApiErro(405, "Método não permitido.");
        }
        Object pedido = finalizar(request);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return pedido;
    }

    /**
     * Corpo: { "itens": [ {idProduto, tamanho, quantidade} ] }
     * Resposta: itens com preço atual, total e avisos (iguais aos do carrinho do site).
     */
    private Object validarCarrinho(HttpServletRequest request) throws Exception {
        Map<String, Object> corpo = lerCorpo(request);
        List<String> avisos = new ArrayList<>();
        Carrinho carrinho = CarrinhoJson.montar(corpo, produtoService, false, avisos);
        avisos.addAll(carrinhoService.atualizar(carrinho));   // estoque e preço atuais
        return CarrinhoJson.resumo(carrinho, new ApiConversor(request), avisos);
    }

    /**
     * Corpo: { "idEndereco": 10, "itens": [ {idProduto, tamanho, quantidade} ] }
     * Resposta 201: o pedido criado (status PENDENTE).
     */
    private Object finalizar(HttpServletRequest request) throws Exception {
        Cliente cliente = exigirCliente(request);
        Map<String, Object> corpo = lerCorpo(request);
        Carrinho carrinho = CarrinhoJson.montar(corpo, produtoService, true, null);

        try {
            Pedido pedido = pedidoService.finalizar(cliente, carrinho, inteiro(corpo, "idEndereco"));
            return new ApiConversor(request).pedido(pedido);

        } catch (ValidacaoException e) {
            // Endereço ou carrinho vazio: dados inválidos (400).
            // Produto esgotado / estoque insuficiente: conflito com o estoque atual (409).
            boolean dadosInvalidos = e.getErros().containsKey("endereco")
                    || "Seu carrinho está vazio.".equals(e.getErros().get("geral"));
            if (dadosInvalidos) {
                throw e;
            }
            throw new ApiErro(409, e.getErros().get("geral"));
        }
    }
}
