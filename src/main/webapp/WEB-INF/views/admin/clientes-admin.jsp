<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- Consulta de clientes (só ADMIN). Somente leitura: o PDF prevê "consultar clientes".
     Dados do ClienteServlet:
     - ${clientes}: lista (com pesquisa ${param.busca});
     - ${cliente} + ${pedidos}: detalhes de um cliente. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Clientes - Admin - Fashion Store</title>
    <c:set var="areaAdmin" value="true" />
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo" class="principal">
<div class="container">
    <div class="cabecalho-pagina">
        <nav class="trilha" aria-label="Você está em"><a href="${ctx}/">Início</a><span aria-hidden="true">/</span><a href="${ctx}/admin">Administração</a></nav>
        <h1>Administração</h1>
    </div>
    <%@ include file="/WEB-INF/views/includes/admin-menu.jsp" %>

    <c:if test="${not empty erroGeral}">
        <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
    </c:if>

    <c:choose>
        <%-- ===================== Detalhes ===================== --%>
        <c:when test="${not empty cliente}">
            <p><a class="texto-suave" href="${ctx}/admin/clientes">&larr; Clientes</a></p>
            <h2><c:out value="${cliente.nome}" /></h2>

            <div class="painel-colunas">
                <section class="cartao cartao-largo">
                    <h3>Dados</h3>
                    <dl class="dados">
                        <dt>E-mail</dt>   <dd><c:out value="${cliente.email}" /></dd>
                        <dt>CPF</dt>      <dd><c:out value="${cliente.cpf}" /></dd>
                        <dt>Telefone</dt> <dd><c:out value="${empty cliente.telefone ? '-' : cliente.telefone}" /></dd>
                    </dl>
                </section>
                <section class="cartao cartao-largo">
                    <h3>Endereços</h3>
                    <c:if test="${empty cliente.enderecos}"><p class="texto-suave">Nenhum endereço.</p></c:if>
                    <ul class="lista-simples">
                        <c:forEach var="e" items="${cliente.enderecos}"><li><c:out value="${e.enderecoFormatado}" /></li></c:forEach>
                    </ul>
                </section>
            </div>

            <h3>Pedidos (${empty pedidos ? 0 : pedidos.size()})</h3>
            <c:choose>
                <c:when test="${empty pedidos}">
                    <p class="texto-suave">Este cliente ainda não fez pedidos.</p>
                </c:when>
                <c:otherwise>
                    <div class="tabela-rolagem">
                        <table class="tabela">
                            <thead><tr><th>Pedido</th><th>Data</th><th>Total</th><th>Status</th></tr></thead>
                            <tbody>
                                <c:forEach var="p" items="${pedidos}">
                                    <tr>
                                        <td><a href="${ctx}/admin/pedidos?id=${p.idPedido}">nº ${p.idPedido}</a></td>
                                        <td>${p.dataFormatada}</td>
                                        <td><fmt:formatNumber value="${p.valorTotal}" type="currency" /></td>
                                        <td><span class="selo status-${p.status}"><c:out value="${p.status.descricao}" /></span></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:when>

        <%-- ===================== Lista ===================== --%>
        <c:otherwise>
            <h2>Clientes</h2>

            <form method="get" action="${ctx}/admin/clientes" class="barra-busca">
                <input type="search" name="busca" maxlength="100" placeholder="Nome, e-mail ou CPF"
                       value="<c:out value='${param.busca}' />" aria-label="Buscar clientes">
                <button type="submit" class="botao">Buscar</button>
                <c:if test="${not empty param.busca}"><a href="${ctx}/admin/clientes" class="link-limpar">Limpar</a></c:if>
            </form>

            <c:if test="${empty erroGeral}">
                <c:choose>
                    <c:when test="${empty clientes}">
                        <div class="vazio"><p>Nenhum cliente encontrado.</p></div>
                    </c:when>
                    <c:otherwise>
                        <p class="texto-suave">${clientes.size()} cliente(s)</p>
                        <div class="tabela-rolagem">
                            <table class="tabela">
                                <thead><tr><th>Nome</th><th>E-mail</th><th>CPF</th><th>Telefone</th><th></th></tr></thead>
                                <tbody>
                                    <c:forEach var="cl" items="${clientes}">
                                        <tr>
                                            <td><c:out value="${cl.nome}" /></td>
                                            <td><c:out value="${cl.email}" /></td>
                                            <td><c:out value="${cl.cpf}" /></td>
                                            <td><c:out value="${empty cl.telefone ? '-' : cl.telefone}" /></td>
                                            <td class="acoes"><a href="${ctx}/admin/clientes?id=${cl.idCliente}">Ver detalhes</a></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
