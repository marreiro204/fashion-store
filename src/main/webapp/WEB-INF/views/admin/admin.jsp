<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- Painel administrativo. Só ADMIN chega aqui (ControleAcessoFilter).
     Dados do AdminServlet: ${resumo} (ResumoPainel) ou ${erroGeral}. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Painel administrativo - Fashion Store</title>
    <c:set var="areaAdmin" value="true" />
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo" class="principal">
<div class="container">
    <div class="cabecalho-pagina">
        <nav class="trilha" aria-label="Você está em"><a href="${ctx}/">Início</a><span aria-hidden="true">/</span><span>Administração</span></nav>
        <h1>Administração</h1>
        <p class="texto-suave">Bem-vindo(a), <c:out value="${sessionScope.usuarioLogado.nome}" />.</p>
    </div>
    <%@ include file="/WEB-INF/views/includes/admin-menu.jsp" %>

    <c:if test="${not empty erroGeral}">
        <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
    </c:if>

    <c:if test="${not empty resumo}">
        <%-- ===================== Números ===================== --%>
        <div class="grade-numeros">
            <a class="numero" href="${ctx}/admin/pedidos?status=PENDENTE">
                <span class="numero-valor">${resumo.pedidosEmAberto}</span>
                <span class="numero-rotulo">pedidos a processar</span>
            </a>
            <a class="numero" href="${ctx}/admin/pedidos">
                <span class="numero-valor">${resumo.totalPedidos}</span>
                <span class="numero-rotulo">pedidos no total</span>
            </a>
            <div class="numero">
                <span class="numero-valor"><fmt:formatNumber value="${resumo.faturamento}" type="currency" /></span>
                <span class="numero-rotulo">faturamento (sem cancelados)</span>
            </div>
            <a class="numero" href="${ctx}/admin/clientes">
                <span class="numero-valor">${resumo.totalClientes}</span>
                <span class="numero-rotulo">clientes</span>
            </a>
            <a class="numero" href="${ctx}/admin/produtos">
                <span class="numero-valor">${resumo.totalProdutos}</span>
                <span class="numero-rotulo">produtos</span>
            </a>
        </div>

        <div class="painel-colunas">
            <section class="cartao">
                <h4>Pedidos por status</h4>
                <ul class="lista-status">
                    <c:forEach var="item" items="${resumo.pedidosPorStatus}">
                        <li>
                            <a href="${ctx}/admin/pedidos?status=${item.key}"><span class="selo status-${item.key}"><c:out value="${item.key.descricao}" /></span></a>
                            <strong>${item.value}</strong>
                        </li>
                    </c:forEach>
                </ul>
            </section>

            <section class="cartao">
                <h4>Alertas de estoque</h4>
                <c:if test="${empty resumo.produtosEsgotados and empty resumo.produtosEstoqueBaixo}">
                    <p class="texto-suave">Nenhum alerta. Todos os produtos têm estoque.</p>
                </c:if>
                <ul class="lista-simples">
                    <c:forEach var="p" items="${resumo.produtosEsgotados}">
                        <li><span class="selo selo-alerta">Esgotado</span>
                            <a class="sublinhado" href="${ctx}/admin/estoque?produto=${p.idProduto}"><c:out value="${p.nome}" /></a></li>
                    </c:forEach>
                    <c:forEach var="p" items="${resumo.produtosEstoqueBaixo}">
                        <li><span class="selo selo-aviso">Estoque baixo</span>
                            <a class="sublinhado" href="${ctx}/admin/estoque?produto=${p.idProduto}"><c:out value="${p.nome}" /></a></li>
                    </c:forEach>
                </ul>
            </section>
        </div>

        <section class="cartao">
            <div class="titulo-acoes">
                <h4>Últimos pedidos</h4>
                <a class="link-seta" href="${ctx}/admin/pedidos">Ver todos</a>
            </div>
            <c:choose>
                <c:when test="${empty resumo.ultimosPedidos}">
                    <p class="texto-suave">Nenhum pedido ainda.</p>
                </c:when>
                <c:otherwise>
                    <div class="tabela-rolagem">
                        <table class="tabela">
                            <thead><tr><th>Pedido</th><th>Data</th><th>Cliente</th><th>Total</th><th>Status</th></tr></thead>
                            <tbody>
                                <c:forEach var="p" items="${resumo.ultimosPedidos}">
                                    <tr>
                                        <td><a href="${ctx}/admin/pedidos?id=${p.idPedido}">nº ${p.idPedido}</a></td>
                                        <td>${p.dataFormatada}</td>
                                        <td><c:out value="${p.cliente.nome}" /></td>
                                        <td><fmt:formatNumber value="${p.valorTotal}" type="currency" /></td>
                                        <td><span class="selo status-${p.status}"><c:out value="${p.status.descricao}" /></span></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </c:if>

    <%-- ===================== Atalhos ===================== --%>
    <div class="titulo-secao"><h2>Gerenciar</h2></div>
    <div class="grade-atalhos">
        <a class="atalho" href="${ctx}/admin/pedidos"><h4>Pedidos</h4><p>Consultar e atualizar status</p></a>
        <a class="atalho" href="${ctx}/admin/produtos"><h4>Produtos</h4><p>Cadastrar, alterar e excluir</p></a>
        <a class="atalho" href="${ctx}/admin/estoque"><h4>Estoque</h4><p>Tamanhos e quantidades</p></a>
        <a class="atalho" href="${ctx}/admin/categorias"><h4>Categorias</h4><p>Organizar o catálogo</p></a>
        <a class="atalho" href="${ctx}/admin/clientes"><h4>Clientes</h4><p>Cadastros e compras</p></a>
    </div>
</div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
