<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Gerenciamento de categorias (só ADMIN).
     Dados do CategoriaServlet: ${categorias}, ${categoria} (edição), ${erros}. --%>
<c:set var="reenvio" value="${pageContext.request.method == 'POST'}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Categorias - Admin - Fashion Store</title>
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

    <h2>Categorias</h2>

    <c:if test="${not empty erroGeral}">
        <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
    </c:if>
    <c:if test="${not empty erros.geral}">
        <div class="alerta alerta-erro"><c:out value="${erros.geral}" /></div>
    </c:if>

    <%-- Formulário: nova categoria ou edição --%>
    <c:set var="idForm" value="${reenvio ? param.id : categoria.idCategoria}" />
    <c:set var="editando" value="${not empty idForm and idForm ne '0'}" />
    <div class="cartao cartao-largo">
        <h3>${editando ? 'Editar categoria' : 'Nova categoria'}</h3>
        <form method="post" action="${ctx}/admin/categorias" class="formulario" novalidate>
            <input type="hidden" name="acao" value="salvar">
            <input type="hidden" name="id" value="<c:out value='${idForm}' />">

            <div class="linha">
                <div>
                    <label for="nome">Nome *</label>
                    <input id="nome" name="nome" maxlength="50" required
                           value="<c:out value='${reenvio ? param.nome : categoria.nome}' />"
                           class="${not empty erros.nome ? 'campo-erro' : ''}">
                    <c:if test="${not empty erros.nome}"><span class="msg-erro"><c:out value="${erros.nome}" /></span></c:if>
                </div>
                <div>
                    <label for="descricao">Descrição</label>
                    <input id="descricao" name="descricao" maxlength="255"
                           value="<c:out value='${reenvio ? param.descricao : categoria.descricao}' />"
                           class="${not empty erros.descricao ? 'campo-erro' : ''}">
                    <c:if test="${not empty erros.descricao}"><span class="msg-erro"><c:out value="${erros.descricao}" /></span></c:if>
                </div>
            </div>

            <div class="acoes-formulario">
                <button type="submit" class="botao">${editando ? 'Salvar alterações' : 'Cadastrar'}</button>
                <c:if test="${editando}">
                    <a href="${ctx}/admin/categorias" class="botao botao-secundario">Cancelar</a>
                </c:if>
            </div>
        </form>
    </div>

    <%-- Lista --%>
    <c:if test="${empty erroGeral}">
        <c:choose>
            <c:when test="${empty categorias}">
                <div class="vazio"><p>Nenhuma categoria cadastrada.</p></div>
            </c:when>
            <c:otherwise>
                <div class="tabela-rolagem">
                    <table class="tabela">
                        <thead>
                            <tr><th>Nome</th><th>Descrição</th><th>Ações</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="cat" items="${categorias}">
                                <tr>
                                    <td><a href="${ctx}/produtos?categoria=${cat.idCategoria}" target="_blank"><c:out value="${cat.nome}" /></a></td>
                                    <td><c:out value="${empty cat.descricao ? '-' : cat.descricao}" /></td>
                                    <td class="acoes">
                                        <a href="${ctx}/admin/categorias?acao=editar&id=${cat.idCategoria}">Editar</a>
                                        <form method="post" action="${ctx}/admin/categorias" class="form-inline"
                                              data-confirmar="Excluir esta categoria?">
                                            <input type="hidden" name="acao" value="excluir">
                                            <input type="hidden" name="id" value="${cat.idCategoria}">
                                            <button type="submit" class="link-perigo">Excluir</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>
</div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
