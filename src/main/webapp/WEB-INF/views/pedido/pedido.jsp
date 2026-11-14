<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Finalização do pedido (cliente logado).
     Dados do PedidoServlet: ${carrinho} (já conferido com o banco), ${enderecos},
     ${enderecoSelecionado}, ${avisos}, ${erros}, ${erroGeral}.
     Envia POST /pedido com o campo "endereco". --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Finalizar pedido - Fashion Store</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo" class="principal">
    <div class="container">
        <div class="cabecalho-pagina">
            <nav class="trilha" aria-label="Você está em">
                <a href="${ctx}/">Início</a><span aria-hidden="true">/</span>
                <a href="${ctx}/carrinho">Carrinho</a><span aria-hidden="true">/</span><span>Finalizar pedido</span>
            </nav>
            <h1>Finalizar pedido</h1>
        </div>

        <c:if test="${not empty erroGeral}">
            <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
        </c:if>
        <c:if test="${not empty erros}">
            <div class="alerta alerta-erro">
                <c:forEach var="erro" items="${erros}"><div><c:out value="${erro.value}" /></div></c:forEach>
            </div>
        </c:if>
        <c:forEach var="aviso" items="${avisos}">
            <div class="alerta alerta-info"><c:out value="${aviso}" /></div>
        </c:forEach>

        <c:if test="${not carrinho.vazio}">
            <form method="post" action="${ctx}/pedido" class="compra">
                <%-- ===================== Endereço ===================== --%>
                <section class="cartao">
                    <h3>Endereço de entrega</h3>
                    <c:choose>
                        <c:when test="${empty enderecos and empty erroGeral}">
                            <div class="alerta alerta-erro">Você não tem endereço cadastrado.</div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="end" items="${enderecos}" varStatus="s">
                                <label class="opcao-endereco">
                                    <input type="radio" name="endereco" value="${end.idEndereco}" required
                                           ${(empty enderecoSelecionado and s.first) or enderecoSelecionado == end.idEndereco ? 'checked' : ''}>
                                    <span><c:out value="${end.enderecoFormatado}" /></span>
                                </label>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>

                    <h3 class="espaco-topo">Itens</h3>
                    <ul class="itens-resumo">
                        <c:forEach var="item" items="${carrinho.itens}">
                            <li>
                                <fs:imagemProduto produto="${item.produto}" classe="miniatura" />
                                <span><c:out value="${item.produto.nome}" /><br>
                                    <span class="texto-suave">Tamanho <c:out value="${item.tamanho}" /> &middot; ${item.quantidade} &times;
                                    <fmt:formatNumber value="${item.precoUnitario}" type="currency" /></span></span>
                                <strong><fmt:formatNumber value="${item.subtotal}" type="currency" /></strong>
                            </li>
                        </c:forEach>
                    </ul>
                </section>

                <%-- ===================== Resumo e confirmação ===================== --%>
                <aside class="cartao resumo">
                    <h3>Resumo</h3>
                    <div class="resumo-linha"><span>Peças</span><span>${carrinho.quantidadeItens}</span></div>
                    <div class="resumo-total"><span>Total</span><strong><fmt:formatNumber value="${carrinho.total}" type="currency" /></strong></div>
                    <button type="submit" class="botao botao-largo" ${empty enderecos ? 'disabled' : ''}>Confirmar pedido</button>
                    <a class="botao botao-secundario botao-largo" href="${ctx}/carrinho">Voltar ao carrinho</a>
                    <p class="texto-suave nota">O pedido fica com status <strong>Pendente</strong> até a loja confirmar o pagamento.</p>
                </aside>
            </form>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
