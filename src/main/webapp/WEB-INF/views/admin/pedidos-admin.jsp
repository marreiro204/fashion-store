<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Gerenciamento de pedidos (só ADMIN).
     Dados do PedidoServlet:
     - ${pedidos} + ${statusFiltro}: lista (filtro por status);
     - ${pedido}: detalhes + formulário de status;
     - ${todosStatus}: todos os valores do enum (para os filtros). --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Pedidos - Admin - Fashion Store</title>
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
        <c:when test="${not empty pedido}">
            <p><a class="texto-suave" href="${ctx}/admin/pedidos">&larr; Pedidos</a></p>
            <div class="titulo-acoes">
                <h2>Pedido nº ${pedido.idPedido}</h2>
                <span class="selo status-${pedido.status}"><c:out value="${pedido.status.descricao}" /></span>
            </div>
            <p class="texto-suave">Feito em ${pedido.dataFormatada}</p>

            <div class="painel-colunas">
                <section class="cartao cartao-largo">
                    <h3>Cliente</h3>
                    <p><a href="${ctx}/admin/clientes?id=${pedido.cliente.idCliente}"><c:out value="${pedido.cliente.nome}" /></a><br>
                       <span class="texto-suave"><c:out value="${pedido.cliente.email}" /></span></p>
                    <h3>Entrega</h3>
                    <p><c:out value="${pedido.endereco.enderecoFormatado}" /></p>
                </section>

                <section class="cartao cartao-largo">
                    <h3>Atualizar status</h3>
                    <c:choose>
                        <%-- Sem próximos status = pedido finalizado (Entregue ou Cancelado).
                             Obs.: não dá para usar ${pedido.status.final}: "final" é palavra
                             reservada do Java e a EL não aceita como nome de propriedade. --%>
                        <c:when test="${empty pedido.status.proximos}">
                            <p class="texto-suave">Este pedido está <strong><c:out value="${pedido.status.descricao}" /></strong>
                               e não pode mais ser alterado.</p>
                        </c:when>
                        <c:otherwise>
                            <form method="post" action="${ctx}/admin/pedidos" class="formulario"
                                  data-confirmar="Cancelar o pedido? As peças voltam para o estoque." data-confirmar-se="status=CANCELADO">
                                <input type="hidden" name="id" value="${pedido.idPedido}">
                                <label for="status">Novo status</label>
                                <select id="status" name="status" required>
                                    <c:forEach var="s" items="${pedido.status.proximos}">
                                        <option value="${s}"><c:out value="${s.descricao}" /></option>
                                    </c:forEach>
                                </select>
                                <button type="submit" class="botao botao-largo">Salvar status</button>
                            </form>
                            <p class="texto-suave">Caminho: Pendente → Pago → Enviado → Entregue.
                               Pendente ou Pago podem ser cancelados.</p>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>

            <div class="tabela-rolagem">
                <table class="tabela">
                    <thead><tr><th></th><th>Produto</th><th>Tamanho</th><th>Preço pago</th><th>Qtd.</th><th>Subtotal</th></tr></thead>
                    <tbody>
                        <c:forEach var="item" items="${pedido.itens}">
                            <tr>
                                <td><fs:imagemProduto produto="${item.produto}" classe="miniatura" /></td>
                                <td><c:out value="${item.produto.nome}" /></td>
                                <td><c:out value="${item.tamanho}" /></td>
                                <td><fmt:formatNumber value="${item.precoUnitario}" type="currency" /></td>
                                <td>${item.quantidade}</td>
                                <td><fmt:formatNumber value="${item.subtotal}" type="currency" /></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            <p class="total">Total: <strong><fmt:formatNumber value="${pedido.valorTotal}" type="currency" /></strong></p>
        </c:when>

        <%-- ===================== Lista ===================== --%>
        <c:otherwise>
            <h2>Pedidos</h2>

            <nav class="filtros" aria-label="Filtrar por status">
                <a href="${ctx}/admin/pedidos" class="filtro ${empty statusFiltro ? 'filtro-ativo' : ''}">Todos</a>
                <c:forEach var="s" items="${todosStatus}">
                    <a href="${ctx}/admin/pedidos?status=${s}" class="filtro ${statusFiltro == s ? 'filtro-ativo' : ''}">
                        <c:out value="${s.descricao}" />
                    </a>
                </c:forEach>
            </nav>

            <c:if test="${empty erroGeral}">
                <c:choose>
                    <c:when test="${empty pedidos}">
                        <div class="vazio"><p>Nenhum pedido encontrado.</p></div>
                    </c:when>
                    <c:otherwise>
                        <div class="tabela-rolagem">
                            <table class="tabela">
                                <thead><tr><th>Pedido</th><th>Data</th><th>Cliente</th><th>Total</th><th>Status</th><th></th></tr></thead>
                                <tbody>
                                    <c:forEach var="p" items="${pedidos}">
                                        <tr>
                                            <td>nº ${p.idPedido}</td>
                                            <td>${p.dataFormatada}</td>
                                            <td><c:out value="${p.cliente.nome}" /></td>
                                            <td><fmt:formatNumber value="${p.valorTotal}" type="currency" /></td>
                                            <td><span class="selo status-${p.status}"><c:out value="${p.status.descricao}" /></span></td>
                                            <td class="acoes"><a href="${ctx}/admin/pedidos?id=${p.idPedido}">Ver / atualizar</a></td>
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
