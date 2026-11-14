<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Controle de estoque por tamanho (só ADMIN).

     Dois modos, decididos pelo EstoqueServlet:
     - ${produtos}: visão geral de todos os produtos;
     - ${produto}:  gerenciar os tamanhos de UM produto.
     Erros: ${erros} (por campo) e ${acaoComErro} (qual formulário falhou). --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Estoque - Admin - Fashion Store</title>
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

    <c:choose>
        <c:when test="${not empty produto}">
            <p><a class="texto-suave" href="${ctx}/admin/estoque">&larr; Estoque de todos os produtos</a></p>
        </c:when>
        <c:otherwise>
        </c:otherwise>
    </c:choose>

    <c:if test="${not empty erroGeral}">
        <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
    </c:if>
    <c:if test="${not empty erros}">
        <div class="alerta alerta-erro">
            <c:forEach var="erro" items="${erros}"><div><c:out value="${erro.value}" /></div></c:forEach>
        </div>
    </c:if>

    <c:choose>
        <%-- ===================== Um produto ===================== --%>
        <c:when test="${not empty produto}">
            <div class="titulo-produto-estoque">
                <fs:imagemProduto produto="${produto}" classe="miniatura" />
                <div>
                    <h2><c:out value="${produto.nome}" /></h2>
                    <span class="texto-suave"><c:out value="${produto.categoria.nome}" /> &middot; Total: ${produto.quantidadeTotal} un.</span>
                </div>
            </div>

            <%-- Novo tamanho --%>
            <c:set var="reenvioAdicionar" value="${acaoComErro == 'adicionar'}" />
            <div class="cartao cartao-largo">
                <h3>Adicionar tamanho</h3>
                <form method="post" action="${ctx}/admin/estoque" class="formulario form-linha" novalidate>
                    <input type="hidden" name="acao" value="adicionar">
                    <input type="hidden" name="produto" value="${produto.idProduto}">
                    <div>
                        <label for="tamanho">Tamanho</label>
                        <input id="tamanho" name="tamanho" maxlength="10" required list="sugestoes-tamanho"
                               placeholder="P, M, G, 40..." value="<c:out value='${reenvioAdicionar ? param.tamanho : ""}' />"
                               class="${reenvioAdicionar and not empty erros.tamanho ? 'campo-erro' : ''}">
                        <datalist id="sugestoes-tamanho">
                            <c:forEach var="t" items="PP,P,M,G,GG,XG,34,36,38,40,42,44,46,48,UNICO">
                                <option value="${t}"></option>
                            </c:forEach>
                        </datalist>
                    </div>
                    <div>
                        <label for="quantidade">Quantidade</label>
                        <input id="quantidade" name="quantidade" type="number" min="0" max="100000" required
                               value="<c:out value='${reenvioAdicionar ? param.quantidade : "0"}' />"
                               class="${reenvioAdicionar and not empty erros.quantidade ? 'campo-erro' : ''}">
                    </div>
                    <div class="form-linha-botao">
                        <button type="submit" class="botao">Adicionar</button>
                    </div>
                </form>
            </div>

            <%-- Tamanhos cadastrados --%>
            <c:choose>
                <c:when test="${empty produto.estoques}">
                    <div class="vazio">
                        <p>Nenhum tamanho cadastrado. Enquanto isso, o produto aparece como <strong>Esgotado</strong> na loja.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="tabela-rolagem">
                        <table class="tabela">
                            <thead>
                                <tr><th>Tamanho</th><th>Quantidade</th><th>Situação</th><th></th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="e" items="${produto.estoques}">
                                    <tr>
                                        <td><strong><c:out value="${e.tamanho}" /></strong></td>
                                        <td>
                                            <form method="post" action="${ctx}/admin/estoque" class="form-quantidade">
                                                <input type="hidden" name="acao" value="alterar">
                                                <input type="hidden" name="produto" value="${produto.idProduto}">
                                                <input type="hidden" name="tamanho" value="<c:out value='${e.tamanho}' />">
                                                <input type="number" name="quantidade" min="0" max="100000" required
                                                       value="${e.quantidade}" aria-label="Quantidade do tamanho <c:out value='${e.tamanho}' />">
                                                <button type="submit" class="botao botao-pequeno">Salvar</button>
                                            </form>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${e.quantidade == 0}"><span class="selo selo-alerta">Esgotado</span></c:when>
                                                <c:when test="${e.estoqueBaixo}"><span class="selo selo-aviso">Estoque baixo</span></c:when>
                                                <c:otherwise><span class="selo selo-ok">Disponível</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="acoes">
                                            <form method="post" action="${ctx}/admin/estoque" class="form-inline"
                                                  data-confirmar="Remover o tamanho deste produto?">
                                                <input type="hidden" name="acao" value="remover">
                                                <input type="hidden" name="idEstoque" value="${e.idEstoque}">
                                                <input type="hidden" name="produto" value="${produto.idProduto}">
                                                <button type="submit" class="link-perigo">Remover</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:when>

        <%-- ===================== Visão geral ===================== --%>
        <c:when test="${empty erroGeral}">
            <h2>Estoque</h2>
            <p class="texto-suave">Selos: <span class="selo selo-alerta">esgotado</span>
               <span class="selo selo-aviso">baixo (até 3 un.)</span> <span class="selo selo-ok">disponível</span></p>
            <c:choose>
                <c:when test="${empty produtos}">
                    <div class="vazio"><p>Nenhum produto cadastrado. <a href="${ctx}/admin/produtos?acao=novo">Cadastre um produto</a>.</p></div>
                </c:when>
                <c:otherwise>
                    <div class="tabela-rolagem">
                        <table class="tabela">
                            <thead>
                                <tr><th></th><th>Produto</th><th>Tamanhos</th><th>Total</th><th></th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${produtos}">
                                    <tr>
                                        <td><fs:imagemProduto produto="${p}" classe="miniatura" /></td>
                                        <td>
                                            <c:out value="${p.nome}" /><br>
                                            <span class="texto-suave"><c:out value="${p.categoria.nome}" /></span>
                                        </td>
                                        <td>
                                            <c:if test="${empty p.estoques}"><span class="texto-suave">Nenhum tamanho</span></c:if>
                                            <c:forEach var="e" items="${p.estoques}">
                                                <span class="selo ${e.quantidade == 0 ? 'selo-alerta' : (e.estoqueBaixo ? 'selo-aviso' : 'selo-ok')}">
                                                    <c:out value="${e.tamanho}" />: ${e.quantidade}
                                                </span>
                                            </c:forEach>
                                        </td>
                                        <td>${p.quantidadeTotal}</td>
                                        <td class="acoes"><a href="${ctx}/admin/estoque?produto=${p.idProduto}">Gerenciar</a></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:when>
    </c:choose>
</div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
