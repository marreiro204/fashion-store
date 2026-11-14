<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Catálogo de produtos com pesquisa e filtro por categoria.
     Dados vindos do ProdutoServlet: ${produtos}, ${categorias} e
     ${categoriaSelecionada} (número já validado, ou vazio). --%>
<fmt:setLocale value="pt_BR" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Produtos - FashionStore</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main class="conteudo">
    <h2>Produtos</h2>

    <%-- GET: a pesquisa aparece na URL, então dá para voltar, favoritar e compartilhar. --%>
    <form method="get" action="${ctx}/produtos" class="barra-busca">
        <input type="search" name="busca" maxlength="100" placeholder="Buscar produtos..."
               value="<c:out value='${param.busca}' />" aria-label="Buscar produtos">
        <select name="categoria" aria-label="Categoria">
            <option value="">Todas as categorias</option>
            <c:forEach var="cat" items="${categorias}">
                <option value="${cat.idCategoria}" ${categoriaSelecionada == cat.idCategoria ? 'selected' : ''}>
                    <c:out value="${cat.nome}" />
                </option>
            </c:forEach>
        </select>
        <button type="submit" class="botao">Buscar</button>
        <c:if test="${not empty param.busca or not empty param.categoria}">
            <a href="${ctx}/produtos" class="link-limpar">Limpar filtros</a>
        </c:if>
    </form>

    <c:if test="${not empty erroGeral}">
        <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
    </c:if>

    <c:if test="${empty erroGeral}">
        <p class="texto-suave">
            ${fn:length(produtos)} produto(s) encontrado(s)
        </p>

        <c:choose>
            <c:when test="${empty produtos}">
                <div class="vazio">
                    <p>Nenhum produto encontrado.</p>
                    <a href="${ctx}/produtos" class="botao">Ver todos os produtos</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="grade-produtos">
                    <c:forEach var="p" items="${produtos}">
                        <a class="card-produto" href="${ctx}/produto?id=${p.idProduto}">
                            <div class="card-imagem-moldura">
                                <fs:imagemProduto produto="${p}" classe="card-imagem" />
                                <c:if test="${p.esgotado}"><span class="selo-esgotado">Esgotado</span></c:if>
                            </div>
                            <span class="card-categoria"><c:out value="${p.categoria.nome}" /></span>
                            <span class="card-nome"><c:out value="${p.nome}" /></span>
                            <span class="card-preco"><fmt:formatNumber value="${p.preco}" type="currency" /></span>
                        </a>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
