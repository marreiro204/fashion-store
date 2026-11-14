<%-- Navegação da área administrativa (aparece no topo de todas as telas do admin).
     Uso: <%@ include file="/WEB-INF/views/includes/admin-menu.jsp" %> --%>
<nav class="admin-menu" aria-label="Administração">
    <a href="${ctx}/admin" class="${rota == '/admin' ? 'ativo' : ''}">Painel</a>
    <a href="${ctx}/admin/pedidos" class="${rota == '/admin/pedidos' ? 'ativo' : ''}">Pedidos</a>
    <a href="${ctx}/admin/produtos" class="${rota == '/admin/produtos' ? 'ativo' : ''}">Produtos</a>
    <a href="${ctx}/admin/estoque" class="${rota == '/admin/estoque' ? 'ativo' : ''}">Estoque</a>
    <a href="${ctx}/admin/categorias" class="${rota == '/admin/categorias' ? 'ativo' : ''}">Categorias</a>
    <a href="${ctx}/admin/clientes" class="${rota == '/admin/clientes' ? 'ativo' : ''}">Clientes</a>
</nav>
