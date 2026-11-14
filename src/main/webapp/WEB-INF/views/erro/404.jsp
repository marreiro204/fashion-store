<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Página de erro 404 (configurada no web.xml).
     Nunca mostramos detalhes técnicos ao usuário: eles ficam no log do Tomcat. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Página não encontrada - Fashion Store</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo" class="principal">
    <div class="container pagina-erro">
        <div class="codigo">404</div>
        <h1>Página não encontrada</h1>
        <p>O endereço acessado não existe ou foi removido.</p>
        <div class="acoes-formulario acoes-centro">
            <a class="botao" href="${ctx}/">Voltar ao início</a>
            <a class="botao botao-secundario" href="${ctx}/produtos">Ver produtos</a>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
