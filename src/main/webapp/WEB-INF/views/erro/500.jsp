<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%-- Não exibimos a exceção para o usuário (segurança).
     O detalhe técnico fica no log do Tomcat (pasta logs/). --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Erro interno - FashionStore</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<main class="conteudo">
    <h2>500 - Ocorreu um erro inesperado</h2>
    <p>Tente novamente em alguns instantes.</p>
    <p><a class="botao" href="${pageContext.request.contextPath}/">Voltar ao início</a></p>
</main>
</body>
</html>
