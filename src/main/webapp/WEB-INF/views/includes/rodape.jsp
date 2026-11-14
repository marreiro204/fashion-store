<%-- Rodapé comum. Uso (antes de </body>):
         <%@ include file="/WEB-INF/views/includes/rodape.jsp" %> --%>
<footer class="rodape">
    <div class="container">
        <div class="rodape-colunas">
            <div class="rodape-marca">
                <img src="${ctx}/assets/images/marca/logo-branca.png" alt="Fashion Store">
                <p>Elegância acessível. Estilo em todos os detalhes.</p>
            </div>
            <div>
                <h4>Loja</h4>
                <ul>
                    <li><a href="${ctx}/produtos">Todos os produtos</a></li>
                    <li><a href="${ctx}/produtos?ordem=novidades">Novidades</a></li>
                    <c:forEach var="cat" items="${categoriasMenu}" end="3">
                        <li><a href="${ctx}/produtos?categoria=${cat.idCategoria}"><c:out value="${cat.nome}" /></a></li>
                    </c:forEach>
                </ul>
            </div>
            <div>
                <h4>Minha conta</h4>
                <ul>
                    <c:choose>
                        <c:when test="${empty sessionScope.usuarioLogado}">
                            <li><a href="${ctx}/login">Entrar</a></li>
                            <li><a href="${ctx}/cadastro">Criar conta</a></li>
                        </c:when>
                        <c:otherwise>
                            <li><a href="${ctx}/pedidos">Meus pedidos</a></li>
                            <li><a href="${ctx}/perfil">Meu perfil</a></li>
                        </c:otherwise>
                    </c:choose>
                    <li><a href="${ctx}/carrinho">Carrinho</a></li>
                </ul>
            </div>
            <div>
                <h4>Sobre</h4>
                <p>Loja virtual desenvolvida no Trabalho Integrador da Uninove, com Java, JSP, Servlets e MySQL.</p>
            </div>
        </div>
        <div class="rodape-final">
            <span>&copy; 2026 Fashion Store</span>
            <span>Trabalho Integrador &middot; Uninove</span>
        </div>
    </div>
</footer>
