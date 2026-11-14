<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<%-- Gerenciamento de produtos (só ADMIN).
     Dados do ProdutoServlet: ${produtos}, ${categorias}, ${produto} (edição),
     ${mostrarFormulario}, ${erros}, ${categoriaSelecionada}.

     De onde vêm os valores do formulário:
     - requisição POST (voltou com erro) -> o que foi digitado (${param.xxx});
     - GET de edição                     -> o produto do banco (${produto.xxx}). --%>
<c:set var="reenvio" value="${pageContext.request.method == 'POST'}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Produtos - Admin - Fashion Store</title>
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

    <div class="titulo-acoes">
        <h2>Produtos</h2>
        <c:if test="${not mostrarFormulario}">
            <a class="botao" href="${ctx}/admin/produtos?acao=novo">+ Novo produto</a>
        </c:if>
    </div>

    <c:if test="${not empty erroGeral}">
        <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
    </c:if>
    <c:if test="${not empty erros.geral}">
        <div class="alerta alerta-erro"><c:out value="${erros.geral}" /></div>
    </c:if>

    <%-- ===================== Formulário ===================== --%>
    <c:if test="${mostrarFormulario}">
        <c:set var="idForm" value="${reenvio ? param.id : produto.idProduto}" />
        <div class="cartao cartao-largo">
            <h3>${empty idForm or idForm eq '0' ? 'Novo produto' : 'Editar produto'}</h3>

            <form method="post" action="${ctx}/admin/produtos" class="formulario" novalidate>
                <input type="hidden" name="acao" value="salvar">
                <input type="hidden" name="id" value="<c:out value='${idForm}' />">

                <label for="nome">Nome *</label>
                <input id="nome" name="nome" maxlength="100" required
                       value="<c:out value='${reenvio ? param.nome : produto.nome}' />"
                       class="${not empty erros.nome ? 'campo-erro' : ''}">
                <c:if test="${not empty erros.nome}"><span class="msg-erro"><c:out value="${erros.nome}" /></span></c:if>

                <div class="linha">
                    <div>
                        <label for="categoria">Categoria *</label>
                        <select id="categoria" name="categoria" required class="${not empty erros.categoria ? 'campo-erro' : ''}">
                            <option value="">Selecione</option>
                            <c:forEach var="cat" items="${categorias}">
                                <option value="${cat.idCategoria}" ${categoriaSelecionada == cat.idCategoria ? 'selected' : ''}>
                                    <c:out value="${cat.nome}" />
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty erros.categoria}"><span class="msg-erro"><c:out value="${erros.categoria}" /></span></c:if>
                        <c:if test="${empty categorias and empty erroGeral}">
                            <span class="texto-suave">Nenhuma categoria cadastrada. <a href="${ctx}/admin/categorias">Cadastre uma</a>.</span>
                        </c:if>
                    </div>
                    <div>
                        <label for="preco">Preço (R$) *</label>
                        <input id="preco" name="preco" inputmode="decimal" required placeholder="59,90"
                               value="<c:out value='${reenvio ? param.preco : produto.preco}' />"
                               class="${not empty erros.preco ? 'campo-erro' : ''}">
                        <c:if test="${not empty erros.preco}"><span class="msg-erro"><c:out value="${erros.preco}" /></span></c:if>
                    </div>
                </div>

                <label for="imagem">Imagem</label>
                <input id="imagem" name="imagem" maxlength="255" placeholder="camiseta-azul.jpg ou https://..."
                       value="<c:out value='${reenvio ? param.imagem : produto.imagem}' />"
                       class="${not empty erros.imagem ? 'campo-erro' : ''}">
                <span class="ajuda">Arquivo em assets/images/produtos/ (ex.: <code>masculino/camisa-social.jpg</code>) ou um endereço https://. Imagens adicionais: mesmo nome com -2, -3...</span>
                <c:if test="${not empty erros.imagem}"><span class="msg-erro"><c:out value="${erros.imagem}" /></span></c:if>

                <label for="descricao">Descrição</label>
                <textarea id="descricao" name="descricao" maxlength="1000" rows="4"
                          class="${not empty erros.descricao ? 'campo-erro' : ''}"><c:out value="${reenvio ? param.descricao : produto.descricao}" /></textarea>
                <c:if test="${not empty erros.descricao}"><span class="msg-erro"><c:out value="${erros.descricao}" /></span></c:if>

                <div class="acoes-formulario">
                    <button type="submit" class="botao">Salvar</button>
                    <a href="${ctx}/admin/produtos" class="botao botao-secundario">Cancelar</a>
                </div>
            </form>
        </div>
    </c:if>

    <%-- ===================== Lista ===================== --%>
    <c:if test="${not mostrarFormulario and empty erroGeral}">
        <c:choose>
            <c:when test="${empty produtos}">
                <div class="vazio"><p>Nenhum produto cadastrado.</p></div>
            </c:when>
            <c:otherwise>
                <div class="tabela-rolagem">
                    <table class="tabela">
                        <thead>
                            <tr>
                                <th></th><th>Nome</th><th>Categoria</th><th>Preço</th><th>Estoque</th><th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${produtos}">
                                <tr>
                                    <td><fs:imagemProduto produto="${p}" classe="miniatura" /></td>
                                    <td><a href="${ctx}/produto?id=${p.idProduto}" target="_blank"><c:out value="${p.nome}" /></a></td>
                                    <td><c:out value="${p.categoria.nome}" /></td>
                                    <td><fmt:formatNumber value="${p.preco}" type="currency" /></td>
                                    <td>
                                        ${p.quantidadeTotal} un.
                                        <c:if test="${p.esgotado}"><span class="selo selo-alerta">Esgotado</span></c:if>
                                    </td>
                                    <td class="acoes">
                                        <a href="${ctx}/admin/produtos?acao=editar&id=${p.idProduto}">Editar</a>
                                        <a href="${ctx}/admin/estoque?produto=${p.idProduto}">Estoque</a>
                                        <form method="post" action="${ctx}/admin/produtos" class="form-inline"
                                              data-confirmar="Excluir este produto? Essa ação não pode ser desfeita.">
                                            <input type="hidden" name="acao" value="excluir">
                                            <input type="hidden" name="id" value="${p.idProduto}">
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
