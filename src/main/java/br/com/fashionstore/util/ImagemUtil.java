package br.com.fashionstore.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import br.com.fashionstore.model.Categoria;
import jakarta.servlet.ServletContext;

/**
 * Localiza as imagens do site nas pastas de assets/images, para que basta
 * COLOCAR o arquivo na pasta certa: nenhum HTML, CSS ou banco precisa mudar.
 *
 *   assets/images/banners/      qualquer imagem vira um slide da página inicial
 *                               (em ordem alfabética: banner-1.jpg, banner-2.jpg...)
 *   assets/images/categorias/   imagem com o "slug" da categoria: camisetas.jpg, calcas.jpg
 *   assets/images/produtos/     imagem principal informada no cadastro do produto
 *                               (ex.: masculino/camisa-social.jpg) e as adicionais
 *                               com -2, -3... no nome (masculino/camisa-social-2.jpg)
 *
 * Todos os métodos devolvem caminhos a partir da raiz do site, por exemplo
 * "/assets/images/banners/banner-1.jpg". Na JSP: ${ctx}${caminho}
 */
public final class ImagemUtil {

    public static final String PASTA_BANNERS = "/assets/images/banners/";
    public static final String PASTA_CATEGORIAS = "/assets/images/categorias/";
    public static final String PASTA_PRODUTOS = "/assets/images/produtos/";

    /** Extensões aceitas, na ordem de preferência. */
    private static final String[] EXTENSOES = {"jpg", "jpeg", "png", "webp"};

    /** Quantas imagens adicionais por produto são procuradas (-2 até -8). */
    private static final int MAXIMO_ADICIONAIS = 8;

    private ImagemUtil() {
    }

    /** Imagens da pasta de banners, em ordem alfabética. Lista vazia se não houver. */
    public static List<String> banners(ServletContext contexto) {
        Set<String> arquivos = contexto.getResourcePaths(PASTA_BANNERS);
        List<String> banners = new ArrayList<>();
        if (arquivos != null) {
            for (String caminho : new TreeSet<>(arquivos)) {
                if (ehImagem(caminho)) {
                    banners.add(caminho);
                }
            }
        }
        return banners;
    }

    /** Imagem da categoria (pelo slug do nome), ou null se não houver arquivo. */
    public static String imagemCategoria(ServletContext contexto, Categoria categoria) {
        return procurar(contexto, PASTA_CATEGORIAS + categoria.getSlug());
    }

    /**
     * Imagens do produto: a principal (do cadastro) seguida das adicionais
     * que existirem na pasta. Para imagem por endereço http(s), devolve só ela.
     * Lista vazia = produto sem imagem (a página mostra a imagem padrão).
     */
    public static List<String> imagensProduto(ServletContext contexto, String imagemPrincipal) {
        List<String> imagens = new ArrayList<>();
        if (imagemPrincipal == null || imagemPrincipal.isBlank()) {
            return imagens;
        }
        if (imagemPrincipal.startsWith("http://") || imagemPrincipal.startsWith("https://")) {
            imagens.add(imagemPrincipal);
            return imagens;
        }
        String principal = PASTA_PRODUTOS + imagemPrincipal;
        imagens.add(principal); // se o arquivo não existir, a tag troca pela imagem padrão

        int ponto = imagemPrincipal.lastIndexOf('.');
        if (ponto > 0) {
            String base = PASTA_PRODUTOS + imagemPrincipal.substring(0, ponto);
            for (int n = 2; n <= MAXIMO_ADICIONAIS; n++) {
                String adicional = procurar(contexto, base + "-" + n);
                if (adicional != null) {
                    imagens.add(adicional);
                }
            }
        }
        return imagens;
    }

    /** Procura "caminho.jpg", "caminho.jpeg", "caminho.png" e "caminho.webp". */
    private static String procurar(ServletContext contexto, String caminhoSemExtensao) {
        for (String ext : EXTENSOES) {
            String caminho = caminhoSemExtensao + "." + ext;
            try {
                if (contexto.getResource(caminho) != null) {
                    return caminho;
                }
            } catch (java.net.MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    private static boolean ehImagem(String caminho) {
        String minusculo = caminho.toLowerCase();
        for (String ext : EXTENSOES) {
            if (minusculo.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }
}
