<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Perfil do cliente (somente leitura nesta etapa).
     Página protegida: o ControleAcessoFilter só deixa chegar aqui quem está logado. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Meu perfil - FashionStore</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main class="conteudo">
    <div class="cartao">
        <h2>Meu perfil</h2>

        <c:if test="${not empty erroGeral}">
            <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
        </c:if>

        <dl class="dados">
            <dt>Nome</dt>     <dd><c:out value="${cliente.nome}" /></dd>
            <dt>E-mail</dt>   <dd><c:out value="${cliente.email}" /></dd>
            <dt>CPF</dt>      <dd><c:out value="${cliente.cpf}" /></dd>
            <dt>Telefone</dt> <dd><c:out value="${empty cliente.telefone ? '-' : cliente.telefone}" /></dd>
            <dt>Perfil</dt>   <dd>${cliente.admin ? 'Administrador' : 'Cliente'}</dd>
        </dl>

        <h3>Endereços</h3>
        <c:choose>
            <c:when test="${empty cliente.enderecos}">
                <p class="texto-suave">Nenhum endereço cadastrado.</p>
            </c:when>
            <c:otherwise>
                <ul class="lista-simples">
                    <c:forEach var="endereco" items="${cliente.enderecos}">
                        <li><c:out value="${endereco.enderecoFormatado}" /></li>
                    </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
