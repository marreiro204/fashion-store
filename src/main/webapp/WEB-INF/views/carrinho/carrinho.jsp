<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Carrinho. Dados do CarrinhoServlet: ${carrinho}, ${avisos}, ${erroGeral}.
     Ações (POST /carrinho): alterar, remover e limpar. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Carrinho - Fashion Store</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo" class="principal">
    <div class="container">
        <div class="cabecalho-pagina">
            <nav class="trilha" aria-label="Você está em"><a href="${ctx}/">Início</a><span aria-hidden="true">/</span><span>Carrinho</span></nav>
            <h1>Carrinho</h1>
        </div>

        <c:if test="${not empty erroGeral}">
            <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
        </c:if>
        <c:forEach var="aviso" items="${avisos}">
            <div class="alerta alerta-info"><c:out value="${aviso}" /></div>
        </c:forEach>

        <c:choose>
            <c:when test="${carrinho.vazio}">
                <div class="vazio">
                    <img src="${ctx}/assets/images/marca/monograma.png" alt="">
                    <h3>Seu carrinho está vazio</h3>
                    <p>Explore a coleção e escolha suas peças favoritas.</p>
                    <a class="botao" href="${ctx}/produtos">Ver produtos</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="compra">
                    <%-- ===================== Itens ===================== --%>
                    <section class="cartao">
                        <c:forEach var="item" items="${carrinho.itens}">
                            <div class="item-carrinho">
                                <a href="${ctx}/produto?id=${item.produto.idProduto}"><fs:imagemProduto produto="${item.produto}" classe="miniatura" /></a>
                                <div>
                                    <a class="item-carrinho-nome" href="${ctx}/produto?id=${item.produto.idProduto}"><c:out value="${item.produto.nome}" /></a>
                                    <div class="item-carrinho-detalhe">
                                        Tamanho <c:out value="${item.tamanho}" /> &middot;
                                        <fmt:formatNumber value="${item.precoUnitario}" type="currency" /> cada
                                    </div>
                                    <form method="post" action="${ctx}/carrinho" class="form-inline">
                                        <input type="hidden" name="acao" value="remover">
                                        <input type="hidden" name="produto" value="${item.produto.idProduto}">
                                        <input type="hidden" name="tamanho" value="<c:out value='${item.tamanho}' />">
                                        <button type="submit" class="link-perigo espaco-pequeno">Remover</button>
                                    </form>
                                </div>
                                <form method="post" action="${ctx}/carrinho" class="form-quantidade">
                                    <input type="hidden" name="acao" value="alterar">
                                    <input type="hidden" name="produto" value="${item.produto.idProduto}">
                                    <input type="hidden" name="tamanho" value="<c:out value='${item.tamanho}' />">
                                    <div class="quantidade quantidade-pequena" data-quantidade>
                                        <button type="button" data-passo="-1" aria-label="Diminuir">&minus;</button>
                                        <input type="number" name="quantidade" value="${item.quantidade}" min="0" max="10" required
                                               aria-label="Quantidade de <c:out value='${item.produto.nome}' />">
                                        <button type="button" data-passo="1" aria-label="Aumentar">+</button>
                                    </div>
                                    <button type="submit" class="botao botao-secundario botao-pequeno">Atualizar</button>
                                </form>
                                <div class="item-carrinho-subtotal"><fmt:formatNumber value="${item.subtotal}" type="currency" /></div>
                            </div>
                        </c:forEach>

                        <div class="acoes-lista">
                            <a class="link-seta" href="${ctx}/produtos">Continuar comprando</a>
                            <form method="post" action="${ctx}/carrinho" class="form-inline" data-confirmar="Esvaziar o carrinho?">
                                <input type="hidden" name="acao" value="limpar">
                                <button type="submit" class="link-perigo">Esvaziar carrinho</button>
                            </form>
                        </div>
                    </section>

                    <%-- ===================== Resumo ===================== --%>
                    <aside class="cartao resumo">
                        <h3>Resumo</h3>
                        <div class="resumo-linha"><span>Peças</span><span>${carrinho.quantidadeItens}</span></div>
                        <div class="resumo-linha"><span>Subtotal</span><span><fmt:formatNumber value="${carrinho.total}" type="currency" /></span></div>
                        <div class="resumo-total"><span>Total</span><strong><fmt:formatNumber value="${carrinho.total}" type="currency" /></strong></div>
                        <a class="botao botao-largo" href="${ctx}/pedido">Finalizar compra</a>
                        <c:if test="${empty sessionScope.usuarioLogado}">
                            <p class="texto-suave texto-centro nota">Você vai entrar na sua conta antes de finalizar. O carrinho é mantido.</p>
                        </c:if>
                    </aside>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
