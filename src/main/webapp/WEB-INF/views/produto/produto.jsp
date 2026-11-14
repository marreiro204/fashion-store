<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Detalhes do produto.
     Dados do ProdutoServlet: ${produto}, ${imagensProduto} (principal + adicionais
     encontradas na pasta) e ${relacionados} (mesma categoria).
     O formulário envia para o CarrinhoServlet (acao=adicionar). --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title><c:out value="${empty produto ? 'Produto' : produto.nome}" /> - Fashion Store</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>
<c:set var="imagemPadrao" value="${ctx}/assets/images/sem-imagem.svg" />

<main id="conteudo" class="principal">
    <div class="container">
        <div class="cabecalho-pagina">
            <nav class="trilha" aria-label="Você está em">
                <a href="${ctx}/">Início</a><span aria-hidden="true">/</span>
                <a href="${ctx}/produtos">Produtos</a>
                <c:if test="${not empty produto}">
                    <span aria-hidden="true">/</span>
                    <a href="${ctx}/produtos?categoria=${produto.categoria.idCategoria}"><c:out value="${produto.categoria.nome}" /></a>
                </c:if>
            </nav>
        </div>

        <c:if test="${not empty erroGeral}">
            <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
        </c:if>

        <c:if test="${not empty produto}">
            <div class="produto-detalhe">
                <%-- ===================== Galeria ===================== --%>
                <div class="galeria ${fn:length(imagensProduto) > 1 ? '' : 'sem-miniaturas'}" data-galeria>
                    <c:if test="${fn:length(imagensProduto) > 1}">
                        <div class="galeria-miniaturas">
                            <c:forEach var="img" items="${imagensProduto}" varStatus="s">
                                <c:set var="urlImg" value="${fn:startsWith(img, 'http') ? img : ctx.concat(img)}" />
                                <button type="button" class="galeria-miniatura ${s.first ? 'ativa' : ''}"
                                        data-imagem="<c:out value='${urlImg}' />" aria-label="Ver imagem ${s.count}">
                                    <img src="<c:out value='${urlImg}' />" alt="" onerror="this.onerror=null;this.src='${imagemPadrao}';">
                                </button>
                            </c:forEach>
                        </div>
                    </c:if>
                    <div class="galeria-principal">
                        <fs:imagemProduto produto="${produto}" />
                    </div>
                </div>

                <%-- ===================== Informações e compra ===================== --%>
                <div class="produto-info">
                    <a class="card-produto-categoria" href="${ctx}/produtos?categoria=${produto.categoria.idCategoria}">
                        <c:out value="${produto.categoria.nome}" />
                    </a>
                    <h1><c:out value="${produto.nome}" /></h1>
                    <p class="produto-preco"><fmt:formatNumber value="${produto.preco}" type="currency" /></p>

                    <c:choose>
                        <c:when test="${empty produto.estoques or produto.esgotado}">
                            <div class="alerta alerta-info">Produto esgotado no momento.</div>
                        </c:when>
                        <c:otherwise>
                            <form method="post" action="${ctx}/carrinho" class="bloco-compra">
                                <input type="hidden" name="acao" value="adicionar">
                                <input type="hidden" name="produto" value="${produto.idProduto}">

                                <fieldset>
                                    <legend>Tamanho</legend>
                                    <div class="tamanhos">
                                        <c:forEach var="e" items="${produto.estoques}">
                                            <label class="tamanho-opcao" title="${e.quantidade == 0 ? 'Esgotado' : (e.estoqueBaixo ? 'Últimas unidades' : 'Disponível')}">
                                                <input type="radio" name="tamanho" value="<c:out value='${e.tamanho}' />" required
                                                       ${e.quantidade == 0 ? 'disabled' : ''}>
                                                <span class="tamanho ${e.quantidade == 0 ? 'tamanho-esgotado' : ''}">
                                                    <c:out value="${e.tamanho}" />
                                                    <c:if test="${e.estoqueBaixo}"><small class="ultimas">últimas</small></c:if>
                                                </span>
                                            </label>
                                        </c:forEach>
                                    </div>
                                </fieldset>

                                <label class="rotulo" for="quantidade">Quantidade</label>
                                <div class="linha-compra">
                                    <div class="quantidade" data-quantidade>
                                        <button type="button" data-passo="-1" aria-label="Diminuir">&minus;</button>
                                        <input type="number" id="quantidade" name="quantidade" value="1" min="1" max="10" required>
                                        <button type="button" data-passo="1" aria-label="Aumentar">+</button>
                                    </div>
                                    <button type="submit" class="botao">Adicionar ao carrinho</button>
                                </div>
                            </form>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty produto.descricao}">
                        <h4>Descrição</h4>
                        <p class="produto-descricao"><c:out value="${produto.descricao}" /></p>
                    </c:if>

                    <ul class="produto-garantias">
                        <li>Estoque conferido por tamanho na finalização do pedido</li>
                        <li>Acompanhe o status do pedido em &ldquo;Meus pedidos&rdquo;</li>
                    </ul>
                </div>
            </div>

            <%-- ===================== Relacionados ===================== --%>
            <c:if test="${not empty relacionados}">
                <div class="titulo-secao">
                    <h2>Você também pode gostar</h2>
                    <a class="link-seta" href="${ctx}/produtos?categoria=${produto.categoria.idCategoria}">Ver categoria</a>
                </div>
                <div class="grade-produtos">
                    <c:forEach var="p" items="${relacionados}">
                        <fs:cardProduto produto="${p}" />
                    </c:forEach>
                </div>
            </c:if>
        </c:if>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
