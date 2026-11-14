<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Perfil do cliente (somente leitura). Página protegida (ControleAcessoFilter).
     Dado do ClienteServlet: ${cliente} (com endereços). --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <title>Meu perfil - Fashion Store</title>
    <%@ include file="/WEB-INF/views/includes/head.jsp" %>
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main id="conteudo" class="principal">
    <div class="container">
        <div class="cabecalho-pagina">
            <nav class="trilha" aria-label="Você está em"><a href="${ctx}/">Início</a><span aria-hidden="true">/</span><span>Minha conta</span></nav>
            <h1>Minha conta</h1>
        </div>

        <div class="conta">
            <%@ include file="/WEB-INF/views/includes/conta-menu.jsp" %>

            <section>
                <c:if test="${not empty erroGeral}">
                    <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
                </c:if>

                <h2>Meu perfil</h2>
                <div class="cartao">
                    <h4>Dados pessoais</h4>
                    <dl class="dados">
                        <dt>Nome</dt>     <dd><c:out value="${cliente.nome}" /></dd>
                        <dt>E-mail</dt>   <dd><c:out value="${cliente.email}" /></dd>
                        <dt>CPF</dt>      <dd><c:out value="${cliente.cpf}" /></dd>
                        <dt>Telefone</dt> <dd><c:out value="${empty cliente.telefone ? '-' : cliente.telefone}" /></dd>
                        <dt>Perfil</dt>   <dd>${cliente.admin ? 'Administrador' : 'Cliente'}</dd>
                    </dl>
                </div>
                <div class="cartao">
                    <h4>Endereços</h4>
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
            </section>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
