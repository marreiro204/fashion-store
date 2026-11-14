<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Catálogo com pesquisa, filtro por categoria e ordenação.
     Dados do ProdutoServlet: ${produtos}, ${categorias},
     ${categoriaSelecionada} (número ou vazio) e ${ordem}. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Produtos - Fashion Store</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<%-- Nome da categoria escolhida, para o título --%>
<c:forEach var="cat" items="${categorias}">
    <c:if test="${categoriaSelecionada == cat.idCategoria}"><c:set var="nomeCategoria" value="${cat.nome}" /></c:if>
</c:forEach>

<main id="conteudo" class="principal">
    <div class="container">
        <div class="cabecalho-pagina">
            <nav class="trilha" aria-label="Você está em">
                <a href="${ctx}/">Início</a><span aria-hidden="true">/</span>
                <a href="${ctx}/produtos">Produtos</a>
                <c:if test="${not empty nomeCategoria}"><span aria-hidden="true">/</span><span><c:out value="${nomeCategoria}" /></span></c:if>
            </nav>
            <h1>
                <c:choose>
                    <c:when test="${not empty nomeCategoria}"><c:out value="${nomeCategoria}" /></c:when>
                    <c:when test="${ordem == 'novidades'}">Novidades</c:when>
                    <c:otherwise>Produtos</c:otherwise>
                </c:choose>
            </h1>
            <c:if test="${empty erroGeral}"><p class="texto-suave">${fn:length(produtos)} produto(s)</p></c:if>
        </div>

        <c:if test="${not empty erroGeral}">
            <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
        </c:if>

        <div class="catalogo">
            <%-- ===================== Filtro lateral ===================== --%>
            <aside class="filtros-lateral" aria-label="Categorias">
                <h4>Categorias</h4>
                <ul>
                    <li><a href="${ctx}/produtos?ordem=${ordem}" class="${empty categoriaSelecionada ? 'ativo' : ''}">Todas</a></li>
                    <c:forEach var="cat" items="${categorias}">
                        <li><a href="${ctx}/produtos?categoria=${cat.idCategoria}&ordem=${ordem}"
                               class="${categoriaSelecionada == cat.idCategoria ? 'ativo' : ''}"><c:out value="${cat.nome}" /></a></li>
                    </c:forEach>
                </ul>
            </aside>

            <section>
                <%-- Busca e ordenação: GET, então os filtros ficam na URL (dá para voltar e compartilhar). --%>
                <form method="get" action="${ctx}/produtos" class="barra-catalogo">
                    <c:if test="${not empty categoriaSelecionada}">
                        <input type="hidden" name="categoria" value="${categoriaSelecionada}">
                    </c:if>
                    <div class="busca">
                        <input class="campo" type="search" name="busca" maxlength="100" placeholder="Buscar nesta lista..."
                               value="<c:out value='${param.busca}' />" aria-label="Buscar produtos">
                        <button type="submit" class="botao">Buscar</button>
                    </div>
                    <label class="ordenar">Ordenar por
                        <select class="campo" name="ordem" onchange="this.form.submit()">
                            <option value="nome" ${ordem == 'nome' ? 'selected' : ''}>Nome</option>
                            <option value="novidades" ${ordem == 'novidades' ? 'selected' : ''}>Novidades</option>
                            <option value="menor-preco" ${ordem == 'menor-preco' ? 'selected' : ''}>Menor preço</option>
                            <option value="maior-preco" ${ordem == 'maior-preco' ? 'selected' : ''}>Maior preço</option>
                        </select>
                    </label>
                </form>

                <c:if test="${not empty param.busca}">
                    <div class="filtros-ativos">
                        <span class="texto-suave">Resultados para</span>
                        <span class="selo">&ldquo;<c:out value="${param.busca}" />&rdquo;</span>
                        <a class="sublinhado texto-suave" href="${ctx}/produtos?ordem=${ordem}${empty categoriaSelecionada ? '' : '&categoria='}${categoriaSelecionada}">Limpar busca</a>
                    </div>
                </c:if>

                <c:if test="${empty erroGeral}">
                    <c:choose>
                        <c:when test="${empty produtos}">
                            <div class="vazio">
                                <h3>Nenhum produto encontrado</h3>
                                <p>Tente outra busca ou veja todos os produtos.</p>
                                <a href="${ctx}/produtos" class="botao">Ver todos os produtos</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="grade-produtos colunas-3">
                                <c:forEach var="p" items="${produtos}">
                                    <fs:cardProduto produto="${p}" />
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </section>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
