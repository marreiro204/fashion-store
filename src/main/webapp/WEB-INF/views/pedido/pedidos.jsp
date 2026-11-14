<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Histórico de compras e detalhes de um pedido (cliente logado).
     Dados do PedidoServlet: ${pedidos} (histórico) ou ${pedido} (detalhes). --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Meus pedidos - Fashion Store</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo" class="principal">
    <div class="container">
        <div class="cabecalho-pagina">
            <nav class="trilha" aria-label="Você está em">
                <a href="${ctx}/">Início</a><span aria-hidden="true">/</span><a href="${ctx}/pedidos">Minha conta</a>
                <c:if test="${not empty pedido}"><span aria-hidden="true">/</span><span>Pedido nº ${pedido.idPedido}</span></c:if>
            </nav>
            <h1>Minha conta</h1>
        </div>

        <div class="conta">
            <%@ include file="/WEB-INF/views/includes/conta-menu.jsp" %>

            <section>
                <c:if test="${not empty erroGeral}">
                    <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
                </c:if>

                <c:choose>
                    <%-- ===================== Detalhes ===================== --%>
                    <c:when test="${not empty pedido}">
                        <div class="titulo-acoes">
                            <div class="cabecalho-pedido">
                                <h2>Pedido nº ${pedido.idPedido}</h2>
                                <span class="selo status-${pedido.status}"><c:out value="${pedido.status.descricao}" /></span>
                            </div>
                            <a class="link-seta" href="${ctx}/pedidos">Todos os pedidos</a>
                        </div>
                        <p class="texto-suave">Feito em ${pedido.dataFormatada}</p>

                        <div class="tabela-rolagem">
                            <table class="tabela">
                                <thead><tr><th></th><th>Produto</th><th>Tamanho</th><th>Preço pago</th><th>Qtd.</th><th>Subtotal</th></tr></thead>
                                <tbody>
                                    <c:forEach var="item" items="${pedido.itens}">
                                        <tr>
                                            <td><fs:imagemProduto produto="${item.produto}" classe="miniatura" /></td>
                                            <td><a href="${ctx}/produto?id=${item.produto.idProduto}"><c:out value="${item.produto.nome}" /></a></td>
                                            <td><c:out value="${item.tamanho}" /></td>
                                            <td><fmt:formatNumber value="${item.precoUnitario}" type="currency" /></td>
                                            <td>${item.quantidade}</td>
                                            <td><fmt:formatNumber value="${item.subtotal}" type="currency" /></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <div class="painel-pedido espaco-topo">
                            <div class="cartao">
                                <h4>Entrega</h4>
                                <p><c:out value="${pedido.endereco.enderecoFormatado}" /></p>
                            </div>
                            <div class="cartao">
                                <div class="resumo-total"><span>Total</span><strong><fmt:formatNumber value="${pedido.valorTotal}" type="currency" /></strong></div>
                            </div>
                        </div>
                    </c:when>

                    <%-- ===================== Histórico ===================== --%>
                    <c:when test="${empty erroGeral}">
                        <h2>Meus pedidos</h2>
                        <c:choose>
                            <c:when test="${empty pedidos}">
                                <div class="vazio">
                                    <h3>Você ainda não fez nenhum pedido</h3>
                                    <p>Quando você finalizar uma compra, ela aparece aqui.</p>
                                    <a class="botao" href="${ctx}/produtos">Ver produtos</a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="tabela-rolagem">
                                    <table class="tabela">
                                        <thead><tr><th>Pedido</th><th>Data</th><th>Total</th><th>Status</th><th></th></tr></thead>
                                        <tbody>
                                            <c:forEach var="p" items="${pedidos}">
                                                <tr>
                                                    <td>nº ${p.idPedido}</td>
                                                    <td>${p.dataFormatada}</td>
                                                    <td><fmt:formatNumber value="${p.valorTotal}" type="currency" /></td>
                                                    <td><span class="selo status-${p.status}"><c:out value="${p.status.descricao}" /></span></td>
                                                    <td class="acoes"><a href="${ctx}/pedidos?id=${p.idPedido}">Ver detalhes</a></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                </c:choose>
            </section>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
