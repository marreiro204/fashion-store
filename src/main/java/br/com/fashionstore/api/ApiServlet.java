package br.com.fashionstore.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.service.ClienteService;
import br.com.fashionstore.service.TokenService;
import br.com.fashionstore.service.ValidacaoException;
import br.com.fashionstore.util.Json;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Classe-mãe de todos os endpoints da API JSON (usada pelo app Flutter).
 *
 * Por que uma classe-mãe? Para que TODOS os endpoints:
 * - respondam em JSON UTF-8;
 * - devolvam erros no mesmo formato: {"erro": "...", "campos": {...}};
 * - tratem banco fora do ar, dados inválidos e falta de login do mesmo jeito.
 *
 * Cada endpoint só implementa get() e/ou post(). A API usa as MESMAS
 * classes Service e DAO do site, então as regras são idênticas.
 *
 * Login na API: token no cabeçalho "Authorization: Bearer <token>"
 * (não usa sessão nem cookie).
 */
public abstract class ApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Tamanho máximo do corpo JSON aceito (64 KB é muito mais do que um pedido precisa). */
    private static final int CORPO_MAXIMO = 64 * 1024;

    protected final ClienteService clienteService = new ClienteService();

    /** GET do endpoint. Padrão: método não permitido. */
    protected Object get(HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new ApiErro(405, "Método não permitido.");
    }

    /** POST do endpoint. Padrão: método não permitido. */
    protected Object post(HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new ApiErro(405, "Método não permitido.");
    }

    /**
     * Ponto central: chama get()/post(), transforma o retorno em JSON e
     * converte qualquer erro na resposta JSON adequada.
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // CORS: permite que o app (inclusive Flutter Web, no navegador) chame a API.
        // O login é por token no cabeçalho, sem cookie, então "*" é seguro aqui.
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Cache-Control", "no-store");

        String metodo = request.getMethod();
        if ("OPTIONS".equals(metodo)) {               // "pré-voo" do navegador
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        try {
            Object corpo = switch (metodo) {
                case "GET" -> get(request, response);
                case "POST" -> post(request, response);
                default -> throw new ApiErro(405, "Método não permitido.");
            };
            int status = response.getStatus() == 0 ? 200 : response.getStatus();
            responder(response, status, corpo);

        } catch (ApiErro e) {
            responderErro(response, e.getStatus(), e.getMessage(), e.getCampos());

        } catch (ValidacaoException e) {
            String mensagem = e.getErros().size() == 1 && e.getErros().containsKey("geral")
                    ? e.getErros().get("geral") : "Corrija os campos destacados.";
            responderErro(response, 400, mensagem, e.getErros());

        } catch (DAOException e) {
            log("Erro de banco na API " + request.getRequestURI(), e);
            responderErro(response, 500, "Não foi possível acessar o banco de dados agora. Tente novamente em instantes.", null);

        } catch (Exception e) {
            log("Erro inesperado na API " + request.getRequestURI(), e);
            responderErro(response, 500, "Erro inesperado no servidor.", null);
        }
    }

    // ========================= Respostas =========================

    protected void responder(HttpServletResponse response, int status, Object corpo) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(Json.escrever(corpo));
    }

    private void responderErro(HttpServletResponse response, int status, String mensagem,
                               Map<String, String> campos) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.resetBuffer();
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", mensagem);
        if (campos != null && !campos.isEmpty()) {
            Map<String, String> semGeral = new LinkedHashMap<>(campos);
            semGeral.remove("geral");
            if (!semGeral.isEmpty()) {
                corpo.put("campos", semGeral);
            }
        }
        responder(response, status, corpo);
    }

    // ========================= Login por token =========================

    /** Token enviado no cabeçalho "Authorization: Bearer <token>" (ou null). */
    protected String token(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return cabecalho.substring(7).trim();
        }
        return null;
    }

    /**
     * Cliente dono do token. Sem token, token inválido ou expirado -> 401
     * (o app deve voltar para a tela de login).
     */
    protected Cliente exigirCliente(HttpServletRequest request) {
        Integer idCliente = TokenService.validar(token(request));
        if (idCliente == null) {
            throw new ApiErro(401, "Faça login para continuar.");
        }
        Cliente cliente = clienteService.buscarPerfil(idCliente);
        if (cliente == null) {
            throw new ApiErro(401, "Faça login para continuar.");
        }
        return cliente;
    }

    // ========================= Leitura do corpo =========================

    /** Lê o corpo da requisição como objeto JSON {...}. JSON inválido -> 400. */
    protected Map<String, Object> lerCorpo(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader leitor = request.getReader()) {
            char[] buffer = new char[4096];
            int lidos;
            while ((lidos = leitor.read(buffer)) != -1) {
                sb.append(buffer, 0, lidos);
                if (sb.length() > CORPO_MAXIMO) {
                    throw new ApiErro(413, "Requisição grande demais.");
                }
            }
        } catch (java.nio.charset.CharacterCodingException e) {
            // Acentos enviados fora do padrão UTF-8: erro de quem enviou, não do servidor.
            throw new ApiErro(400, "Envie o JSON codificado em UTF-8.");
        }
        if (sb.toString().isBlank()) {
            throw new ApiErro(400, "Envie os dados em JSON no corpo da requisição.");
        }
        try {
            return Json.lerObjeto(sb.toString());
        } catch (IllegalArgumentException e) {
            throw new ApiErro(400, "JSON inválido: " + e.getMessage());
        }
    }

    /** Campo de texto do JSON (ou null). */
    protected static String texto(Map<String, Object> dados, String campo) {
        Object valor = dados.get(campo);
        return valor == null ? null : String.valueOf(valor);
    }

    /** Campo numérico inteiro do JSON (aceita 2 ou "2"); ausente/inválido -> null. */
    protected static Integer inteiro(Map<String, Object> dados, String campo) {
        Object valor = dados.get(campo);
        try {
            if (valor instanceof BigDecimal n) {
                return n.intValueExact();
            }
            if (valor instanceof String s) {
                return Integer.valueOf(s.trim());
            }
        } catch (ArithmeticException | NumberFormatException e) {
            return null;
        }
        return null;
    }

    /** Parte da URL depois do endpoint: "/api/produtos/5" -> 5; sem número -> null; texto -> 404. */
    protected static Integer idDaUrl(HttpServletRequest request) {
        String info = request.getPathInfo();
        if (info == null || info.equals("/")) {
            return null;
        }
        try {
            return Integer.valueOf(info.substring(1));
        } catch (NumberFormatException e) {
            throw new ApiErro(404, "Recurso não encontrado.");
        }
    }
}
