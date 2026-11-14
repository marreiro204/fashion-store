<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Entrar - FashionStore</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main class="conteudo">
    <div class="cartao cartao-estreito">
        <h2>Entrar</h2>

        <c:if test="${not empty erro}">
            <div class="alerta alerta-erro"><c:out value="${erro}" /></div>
        </c:if>

        <form method="post" action="${ctx}/login" class="formulario" novalidate>
            <%-- Página que o usuário tentou abrir antes de fazer login --%>
            <input type="hidden" name="redirect" value="<c:out value='${param.redirect}' />">

            <label for="email">E-mail</label>
            <input type="email" id="email" name="email" maxlength="150" required autofocus
                   autocomplete="username" value="<c:out value='${param.email}' />">

            <label for="senha">Senha</label>
            <input type="password" id="senha" name="senha" maxlength="72" required
                   autocomplete="current-password">

            <button type="submit" class="botao botao-largo">Entrar</button>
        </form>

        <p class="texto-centro">Ainda não tem conta? <a href="${ctx}/cadastro">Cadastre-se</a></p>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
