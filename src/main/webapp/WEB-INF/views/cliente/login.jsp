<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Login. Envia POST /login (email, senha e a página de retorno "redirect"). --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Entrar - Fashion Store</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo" class="principal">
    <div class="container acesso">
        <div class="cartao acesso-cartao">
            <img class="acesso-marca" src="${ctx}/assets/images/marca/monograma.png" alt="">
            <h1>Entrar</h1>
            <p class="subtitulo">Acesse sua conta para finalizar compras e acompanhar pedidos.</p>

            <c:if test="${not empty erro}">
                <div class="alerta alerta-erro"><c:out value="${erro}" /></div>
            </c:if>

            <form method="post" action="${ctx}/login" class="formulario" novalidate>
                <%-- Página que o usuário tentou abrir antes do login --%>
                <input type="hidden" name="redirect" value="<c:out value='${param.redirect}' />">

                <label for="email">E-mail</label>
                <input type="email" id="email" name="email" maxlength="150" required autofocus
                       autocomplete="username" value="<c:out value='${param.email}' />">

                <label for="senha">Senha</label>
                <input type="password" id="senha" name="senha" maxlength="72" required autocomplete="current-password">

                <div class="acoes-formulario">
                    <button type="submit" class="botao botao-largo">Entrar</button>
                </div>
            </form>

            <p class="acesso-rodape">Ainda não tem conta? <a href="${ctx}/cadastro">Criar conta</a></p>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
