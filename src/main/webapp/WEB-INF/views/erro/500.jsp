<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Página de erro 500 (configurada no web.xml).
     Nunca mostramos detalhes técnicos ao usuário: eles ficam no log do Tomcat. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Algo deu errado - Fashion Store</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo" class="principal">
    <div class="container pagina-erro">
        <div class="codigo">500</div>
        <h1>Algo deu errado</h1>
        <p>Ocorreu um erro inesperado. Tente novamente em instantes.</p>
        <div class="acoes-formulario acoes-centro">
            <a class="botao" href="${ctx}/">Voltar ao início</a>
            <a class="botao botao-secundario" href="${ctx}/produtos">Ver produtos</a>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
