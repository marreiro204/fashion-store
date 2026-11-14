<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Detalhes do produto. Dado vindo do ProdutoServlet: ${produto}.
     A seleção de tamanho/quantidade e o botão "Adicionar ao carrinho"
     passam a funcionar na Etapa 7. --%>
<fmt:setLocale value="pt_BR" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${empty produto ? 'Produto' : produto.nome}" /> - FashionStore</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main class="conteudo">
    <p><a href="${ctx}/produtos">&larr; Voltar ao catálogo</a></p>

    <c:if test="${not empty erroGeral}">
        <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
    </c:if>

    <c:if test="${not empty produto}">
        <div class="detalhe-produto">
            <div class="detalhe-imagem">
                <fs:imagemProduto produto="${produto}" classe="imagem-grande" />
            </div>

            <div class="detalhe-info">
                <a class="card-categoria" href="${ctx}/produtos?categoria=${produto.categoria.idCategoria}">
                    <c:out value="${produto.categoria.nome}" />
                </a>
                <h1><c:out value="${produto.nome}" /></h1>
                <p class="detalhe-preco"><fmt:formatNumber value="${produto.preco}" type="currency" /></p>

                <h3>Tamanhos</h3>
                <c:choose>
                    <c:when test="${empty produto.estoques}">
                        <p class="texto-suave">Tamanhos ainda não cadastrados.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="tamanhos">
                            <c:forEach var="e" items="${produto.estoques}">
                                <span class="tamanho ${e.quantidade == 0 ? 'tamanho-esgotado' : ''}"
                                      title="${e.quantidade == 0 ? 'Esgotado' : (e.estoqueBaixo ? 'Últimas unidades' : 'Disponível')}">
                                    <c:out value="${e.tamanho}" />
                                    <c:if test="${e.estoqueBaixo}"><small class="ultimas">últimas</small></c:if>
                                </span>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${produto.esgotado}">
                        <p class="alerta alerta-erro">Produto esgotado no momento.</p>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="botao botao-largo" disabled
                                title="O carrinho será ativado na Etapa 7">Adicionar ao carrinho (em breve)</button>
                    </c:otherwise>
                </c:choose>

                <c:if test="${not empty produto.descricao}">
                    <h3>Descrição</h3>
                    <p class="detalhe-descricao"><c:out value="${produto.descricao}" /></p>
                </c:if>
            </div>
        </div>
    </c:if>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
