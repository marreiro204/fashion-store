<%-- Cabeçalho comum a todas as páginas.
     Uso: <%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

     IMPORTANTE (segurança): tudo que veio do usuário é exibido com <c:out>,
     que troca < > " & por códigos HTML. Isso impede que alguém cadastre um
     nome como <script>...</script> e execute código no navegador de outra
     pessoa (ataque XSS). --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<header class="topo">
    <div class="topo-conteudo">
        <a class="logo" href="${ctx}/">FashionStore</a>
        <nav class="menu">
            <a href="${ctx}/">Início</a>
            <a href="${ctx}/produtos">Produtos</a>
            <c:choose>
                <c:when test="${empty sessionScope.usuarioLogado}">
                    <a href="${ctx}/login">Entrar</a>
                    <a class="botao botao-pequeno" href="${ctx}/cadastro">Criar conta</a>
                </c:when>
                <c:otherwise>
                    <span class="saudacao">Olá, <c:out value="${sessionScope.usuarioLogado.nome}" /></span>
                    <a href="${ctx}/perfil">Meu perfil</a>
                    <c:if test="${sessionScope.usuarioLogado.admin}">
                        <a href="${ctx}/admin">Painel admin</a>
                    </c:if>
                    <form class="form-inline" method="post" action="${ctx}/logout">
                        <button type="submit" class="link-botao">Sair</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
</header>

<%-- Mensagem de uma vez só (ex.: "Cadastro realizado!"): mostra e apaga. --%>
<c:if test="${not empty sessionScope.mensagem}">
    <div class="conteudo">
        <div class="alerta alerta-info"><c:out value="${sessionScope.mensagem}" /></div>
    </div>
    <c:remove var="mensagem" scope="session" />
</c:if>
