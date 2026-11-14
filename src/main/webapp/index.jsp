<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Página inicial. Na Etapa 5 ela passa a mostrar os produtos em destaque. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FashionStore - Loja Virtual de Roupas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main class="conteudo">
    <section class="destaque">
        <h1>Moda para todos os estilos</h1>
        <p>Camisetas, calças, vestidos, jaquetas e muito mais.</p>
        <a class="botao" href="${ctx}/produtos">Ver produtos</a>
        <c:if test="${empty sessionScope.usuarioLogado}">
            <a class="botao botao-contorno" href="${ctx}/cadastro">Criar minha conta</a>
        </c:if>
    </section>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
