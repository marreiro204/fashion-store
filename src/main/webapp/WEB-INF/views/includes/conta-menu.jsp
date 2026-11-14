<%-- Menu lateral da área do cliente (perfil e pedidos).
     Uso: <%@ include file="/WEB-INF/views/includes/conta-menu.jsp" %> --%>
<ul class="conta-menu">
    <li><a href="${ctx}/pedidos" class="${rota == '/pedidos' ? 'ativo' : ''}">Meus pedidos</a></li>
    <li><a href="${ctx}/perfil" class="${rota == '/perfil' ? 'ativo' : ''}">Meu perfil</a></li>
    <li><a href="${ctx}/carrinho">Carrinho</a></li>
    <c:if test="${sessionScope.usuarioLogado.admin}"><li><a href="${ctx}/admin">Painel administrativo</a></li></c:if>
    <li>
        <form class="form-inline" method="post" action="${ctx}/logout"><button type="submit">Sair</button></form>
    </li>
</ul>
