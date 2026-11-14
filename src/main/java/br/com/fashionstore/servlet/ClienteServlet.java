package br.com.fashionstore.servlet;

import java.io.IOException;

import br.com.fashionstore.dao.DAOException;
import br.com.fashionstore.model.Cliente;
import br.com.fashionstore.model.Endereco;
import br.com.fashionstore.service.ClienteService;
import br.com.fashionstore.service.PedidoService;
import br.com.fashionstore.service.ValidacaoException;
import br.com.fashionstore.util.SessaoUtil;
import br.com.fashionstore.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cadastro e perfil do cliente.
 *
 *   GET  /cadastro -> formulário de cadastro
 *   POST /cadastro -> cadastra e já faz o login
 *   GET  /perfil   -> dados do cliente logado (página protegida pelo filtro)
 *
 * Área administrativa (só ADMIN):
 *   GET  /admin/clientes?busca=ana -> consulta de clientes
 *   GET  /admin/clientes?id=3      -> dados, endereços e pedidos de um cliente
 */
@WebServlet(urlPatterns = {"/cadastro", "/perfil", "/admin/clientes"})
public class ClienteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW_CADASTRO = "/WEB-INF/views/cliente/cadastro.jsp";
    private static final String VIEW_PERFIL = "/WEB-INF/views/cliente/perfil.jsp";
    private static final String VIEW_ADMIN = "/WEB-INF/views/admin/clientes-admin.jsp";

    private final ClienteService clienteService = new ClienteService();
    private final PedidoService pedidoService = new PedidoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("/perfil".equals(request.getServletPath())) {
            mostrarPerfil(request, response);
            return;
        }
        if ("/admin/clientes".equals(request.getServletPath())) {
            adminClientes(request, response);
            return;
        }

        if (SessaoUtil.getUsuarioLogado(request) != null) {
            response.sendRedirect(request.getContextPath() + "/perfil"); // já tem conta
            return;
        }
        request.getRequestDispatcher(VIEW_CADASTRO).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"/cadastro".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        // 1. Lê os campos do formulário
        Cliente cliente = new Cliente(
                request.getParameter("nome"),
                request.getParameter("email"),
                null,
                request.getParameter("cpf"),
                request.getParameter("telefone"));

        Endereco endereco = new Endereco(0,
                request.getParameter("cep"),
                request.getParameter("logradouro"),
                request.getParameter("numero"),
                request.getParameter("complemento"),
                request.getParameter("bairro"),
                request.getParameter("cidade"),
                request.getParameter("estado"));

        // 2. Pede ao Service para cadastrar (as regras ficam lá)
        try {
            Cliente cadastrado = clienteService.cadastrar(cliente,
                    request.getParameter("senha"), request.getParameter("confirmacao"), endereco);

            // 3. Sucesso: já entra logado e vai para a página inicial
            SessaoUtil.login(request, cadastrado);
            SessaoUtil.mensagem(request, "Cadastro realizado! Bem-vindo(a), " + cadastrado.getNome() + ".");
            response.sendRedirect(request.getContextPath() + "/");

        } catch (ValidacaoException e) {
            // Volta ao formulário mostrando os erros. Os valores digitados
            // reaparecem pelos ${param.xxx} da JSP (exceto as senhas).
            request.setAttribute("erros", e.getErros());
            request.getRequestDispatcher(VIEW_CADASTRO).forward(request, response);

        } catch (DAOException e) {
            log("Erro de banco no cadastro", e);
            request.setAttribute("erroGeral",
                    "Não foi possível concluir o cadastro agora. Tente novamente em instantes.");
            request.getRequestDispatcher(VIEW_CADASTRO).forward(request, response);
        }
    }

    /** Consulta de clientes (lista com pesquisa, ou detalhes de um cliente). */
    private void adminClientes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idTexto = request.getParameter("id");
        try {
            if (idTexto != null) {
                Integer id = Validador.converterInteiro(idTexto);
                Cliente cliente = id == null ? null : clienteService.buscarPerfil(id);
                if (cliente == null) {
                    SessaoUtil.mensagem(request, "Cliente não encontrado.");
                    response.sendRedirect(request.getContextPath() + "/admin/clientes");
                    return;
                }
                request.setAttribute("cliente", cliente);
                request.setAttribute("pedidos", pedidoService.listarHistorico(id));
            } else {
                request.setAttribute("clientes", clienteService.listarClientes(request.getParameter("busca")));
            }
        } catch (DAOException e) {
            log("Erro de banco na consulta de clientes", e);
            request.setAttribute("erroGeral", "Não foi possível acessar o banco de dados agora. Tente novamente em instantes.");
        }
        request.getRequestDispatcher(VIEW_ADMIN).forward(request, response);
    }

    private void mostrarPerfil(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Cliente logado = SessaoUtil.getUsuarioLogado(request); // o filtro garante que existe
        try {
            Cliente perfil = clienteService.buscarPerfil(logado.getIdCliente());
            request.setAttribute("cliente", perfil != null ? perfil : logado);
        } catch (DAOException e) {
            log("Erro de banco no perfil", e);
            request.setAttribute("cliente", logado); // mostra ao menos os dados da sessão
            request.setAttribute("erroGeral", "Não foi possível carregar todos os seus dados agora.");
        }
        request.getRequestDispatcher(VIEW_PERFIL).forward(request, response);
    }
}
