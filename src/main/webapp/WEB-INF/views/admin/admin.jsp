<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Painel administrativo (versão inicial). Só ADMIN chega aqui (ControleAcessoFilter).
     As seções serão ativadas nas próximas etapas. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Painel administrativo - FashionStore</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main class="conteudo">
    <h2>Painel administrativo</h2>
    <p class="texto-suave">Bem-vindo(a), <c:out value="${sessionScope.usuarioLogado.nome}" />.</p>

    <div class="grade-cartoes">
        <a class="cartao cartao-menu cartao-link" href="${ctx}/admin/produtos"><h3>Produtos</h3><p class="texto-suave">Cadastrar, alterar e excluir</p></a>
        <a class="cartao cartao-menu cartao-link" href="${ctx}/admin/categorias"><h3>Categorias</h3><p class="texto-suave">Organizar o catálogo</p></a>
        <a class="cartao cartao-menu cartao-link" href="${ctx}/admin/estoque"><h3>Estoque</h3><p class="texto-suave">Tamanhos e quantidades</p></a>
        <div class="cartao cartao-menu"><h3>Clientes</h3><p class="texto-suave">Disponível na Etapa 8</p></div>
        <div class="cartao cartao-menu"><h3>Pedidos</h3><p class="texto-suave">Disponível na Etapa 8</p></div>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
