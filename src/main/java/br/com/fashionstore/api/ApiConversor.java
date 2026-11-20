package br.com.fashionstore.api;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.fashionstore.model.Categoria;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.model.Estoque;
import br.com.fashionstore.model.ItemPedido;
import br.com.fashionstore.model.Pedido;
import br.com.fashionstore.model.Produto;
import br.com.fashionstore.util.ImagemUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Converte os objetos Java (Model) no JSON combinado com o app Flutter.
 *
 * Os nomes dos campos são os MESMOS das classes Java (idProduto, preco...),
 * como está no Guia do App Flutter. Dados sensíveis (senha) nunca saem.
 *
 * As imagens saem com o endereço COMPLETO (http://servidor:8080/FashionStore/...),
 * montado a partir do endereço que o próprio app usou para chamar a API.
 * Assim funciona no emulador (10.0.2.2) e no celular (IP do computador).
 */
public final class ApiConversor {

    private final ServletContext contexto;
    private final String baseUrl;

    public ApiConversor(HttpServletRequest request) {
        this.contexto = request.getServletContext();
        this.baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath();
    }

    /** Construtor para testes (sem requisição). */
    ApiConversor(ServletContext contexto, String baseUrl) {
        this.contexto = contexto;
        this.baseUrl = baseUrl;
    }

    public Map<String, Object> categoria(Categoria c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idCategoria", c.getIdCategoria());
        m.put("nome", c.getNome());
        m.put("descricao", c.getDescricao());
        return m;
    }

    public Map<String, Object> produto(Produto p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idProduto", p.getIdProduto());
        m.put("nome", p.getNome());
        m.put("descricao", p.getDescricao());
        m.put("preco", p.getPreco());
        m.put("categoria", p.getCategoria() == null ? null : categoria(p.getCategoria()));
        m.put("imagens", imagens(p.getImagem()));

        List<Map<String, Object>> tamanhos = new ArrayList<>();
        for (Estoque e : p.getEstoques()) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("tamanho", e.getTamanho());
            t.put("quantidade", e.getQuantidade());
            t.put("ultimas", e.isEstoqueBaixo());     // mostrar "últimas" como no site
            tamanhos.add(t);
        }
        m.put("tamanhos", tamanhos);
        m.put("esgotado", p.isEsgotado());
        return m;
    }

    public Map<String, Object> endereco(Endereco e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idEndereco", e.getIdEndereco());
        m.put("cep", e.getCep());
        m.put("logradouro", e.getLogradouro());
        m.put("numero", e.getNumero());
        m.put("complemento", e.getComplemento());
        m.put("bairro", e.getBairro());
        m.put("cidade", e.getCidade());
        m.put("estado", e.getEstado());
        m.put("enderecoFormatado", e.getEnderecoFormatado());
        return m;
    }

    /** Cliente SEM senha. */
    public Map<String, Object> cliente(Cliente c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idCliente", c.getIdCliente());
        m.put("nome", c.getNome());
        m.put("email", c.getEmail());
        m.put("cpf", c.getCpf());
        m.put("telefone", c.getTelefone());
        m.put("perfil", c.getPerfil());
        List<Map<String, Object>> enderecos = new ArrayList<>();
        for (Endereco e : c.getEnderecos()) {
            enderecos.add(endereco(e));
        }
        m.put("enderecos", enderecos);
        return m;
    }

    public Map<String, Object> item(ItemPedido i) {
        Map<String, Object> produto = new LinkedHashMap<>();
        produto.put("idProduto", i.getProduto().getIdProduto());
        produto.put("nome", i.getProduto().getNome());
        List<String> imagens = imagens(i.getProduto().getImagem());
        produto.put("imagem", imagens.isEmpty() ? null : imagens.get(0));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("produto", produto);
        m.put("tamanho", i.getTamanho());
        m.put("quantidade", i.getQuantidade());
        m.put("precoUnitario", i.getPrecoUnitario());
        m.put("subtotal", i.getSubtotal());
        return m;
    }

    public Map<String, Object> pedido(Pedido p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idPedido", p.getIdPedido());
        m.put("dataPedido", p.getDataPedido());
        m.put("status", p.getStatus());
        m.put("statusDescricao", p.getStatus().getDescricao());
        m.put("valorTotal", p.getValorTotal());
        m.put("endereco", p.getEndereco() == null ? null : endereco(p.getEndereco()));
        List<Map<String, Object>> itens = new ArrayList<>();
        for (ItemPedido i : p.getItens()) {
            itens.add(item(i));
        }
        m.put("itens", itens);   // vazio na listagem (histórico); completo nos detalhes
        return m;
    }

    /**
     * Endereços completos das imagens do produto (principal + adicionais).
     * Arquivos que não existem são ignorados: lista vazia = o app mostra
     * a imagem provisória.
     */
    public List<String> imagens(String imagemPrincipal) {
        List<String> urls = new ArrayList<>();
        for (String caminho : ImagemUtil.imagensProduto(contexto, imagemPrincipal)) {
            if (caminho.startsWith("http://") || caminho.startsWith("https://")) {
                urls.add(caminho);
            } else if (existe(caminho)) {
                urls.add(baseUrl + caminho);
            }
        }
        return urls;
    }

    private boolean existe(String caminho) {
        try {
            return contexto.getResource(caminho) != null;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
