<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Página inicial (HomeServlet).
     Dados: ${banners} (fotos da pasta assets/images/banners/), ${categorias},
     ${imagensCategorias} (slug -> imagem), ${novidades} (últimos produtos).

     Banners: coloque as fotos em assets/images/banners/ (banner-1.jpg,
     banner-2.jpg...). Cada foto vira um slide. Sem fotos, aparece um fundo
     provisório com o monograma. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Fashion Store - Elegância acessível</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo">
    <%-- ===================== Banner principal ===================== --%>
    <section class="banner" data-carrossel aria-label="Destaques">
        <c:choose>
            <c:when test="${empty banners}">
                <div class="banner-slide ativo">
                    <div class="banner-provisorio">
                        <img src="${ctx}/assets/images/marca/monograma-branco.png" alt="">
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:forEach var="banner" items="${banners}" varStatus="s">
                    <div class="banner-slide ${s.first ? 'ativo' : ''}">
                        <img src="${ctx}${banner}" alt="" ${s.first ? '' : 'loading="lazy"'}>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>

        <div class="banner-texto">
            <div class="container">
                <h1 class="banner-titulo">Fashion<br>Store</h1>
                <p class="banner-subtitulo">Elegância acessível.<br>Estilo em todos os detalhes.</p>
                <a class="botao botao-claro" href="${ctx}/produtos"><span class="link-seta">Explorar Coleção</span></a>
            </div>
        </div>

        <c:if test="${banners.size() > 1}">
            <div class="banner-pontos">
                <c:forEach var="banner" items="${banners}" varStatus="s">
                    <button type="button" class="banner-ponto ${s.first ? 'ativo' : ''}" aria-label="Destaque ${s.count}"></button>
                </c:forEach>
            </div>
        </c:if>
    </section>

    <div class="container">
        <c:if test="${not empty erroGeral}">
            <div class="alerta alerta-erro espaco-topo"><c:out value="${erroGeral}" /></div>
        </c:if>

        <%-- ===================== Categorias ===================== --%>
        <c:if test="${not empty categorias}">
            <div class="titulo-secao">
                <h2>Categorias</h2>
                <a class="link-seta" href="${ctx}/produtos">Ver todas</a>
            </div>
            <div class="grade-categorias">
                <c:forEach var="cat" items="${categorias}">
                    <a class="card-categoria" href="${ctx}/produtos?categoria=${cat.idCategoria}">
                        <c:choose>
                            <c:when test="${not empty imagensCategorias[cat.slug]}">
                                <img src="${ctx}${imagensCategorias[cat.slug]}" alt="" loading="lazy">
                            </c:when>
                            <c:otherwise>
                                <img class="card-categoria-marca" src="${ctx}/assets/images/marca/monograma-branco.png" alt="">
                            </c:otherwise>
                        </c:choose>
                        <span class="card-categoria-texto">
                            <span class="card-categoria-nome"><c:out value="${cat.nome}" /></span>
                            <span class="card-categoria-seta" aria-hidden="true">&rarr;</span>
                        </span>
                    </a>
                </c:forEach>
            </div>
        </c:if>

        <%-- ===================== Novidades ===================== --%>
        <c:if test="${not empty novidades}">
            <div class="titulo-secao">
                <h2>Novidades</h2>
                <a class="link-seta" href="${ctx}/produtos?ordem=novidades">Ver todas</a>
            </div>
            <div class="grade-produtos">
                <c:forEach var="p" items="${novidades}">
                    <fs:cardProduto produto="${p}" />
                </c:forEach>
            </div>
        </c:if>

        <c:if test="${empty categorias and empty novidades and empty erroGeral}">
            <div class="vazio espaco-topo">
                <h3>A coleção está chegando</h3>
                <p>Em breve os produtos estarão disponíveis aqui.</p>
            </div>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
