<%-- Cabeçalho comum a todas as páginas: barra de aviso, logo, menu,
     busca, conta e carrinho. Uso (logo após <body>):
         <%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

     O menu mostra as categorias do BANCO (${categoriasMenu}, colocadas pelo
     MenuFilter). Para "Masculino" e "Feminino" aparecerem como na
     referência, basta o admin cadastrar essas categorias.

     Segurança: tudo que vem do usuário ou do banco é exibido com <c:out>
     (evita XSS). --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<%-- Caminho original pedido (antes do forward para a JSP): "" = início, "/produtos"... --%>
<c:set var="rota" value="${requestScope['jakarta.servlet.forward.servlet_path']}" />
<c:set var="noCatalogo" value="${rota == '/produtos'}" />

<%-- ===================== Barra de aviso =====================
     Para trocar os textos, edite as frases abaixo. --%>
<div class="aviso" data-aviso>
    <div class="container aviso-conteudo">
        <button type="button" class="aviso-seta" data-aviso-anterior aria-label="Mensagem anterior"><fs:icone nome="anterior" /></button>
        <div>
            <p class="aviso-mensagem">Nova coleção disponível &mdash; <a href="${ctx}/produtos?ordem=novidades">confira as novidades</a></p>
            <p class="aviso-mensagem" hidden>Escolha seu tamanho e acompanhe seus pedidos pela sua conta</p>
            <p class="aviso-mensagem" hidden>Estoque atualizado por tamanho, direto da loja</p>
        </div>
        <button type="button" class="aviso-seta" data-aviso-proximo aria-label="Próxima mensagem"><fs:icone nome="proximo" /></button>
    </div>
</div>

<%-- ===================== Cabeçalho ===================== --%>
<header class="topo">
    <div class="container topo-conteudo">
        <a class="logo" href="${ctx}/" aria-label="Fashion Store - página inicial">
            <img src="${ctx}/assets/images/marca/logo-texto.png" alt="Fashion Store">
        </a>

        <nav class="menu" aria-label="Menu principal">
            <a href="${ctx}/" class="${rota == '' ? 'ativo' : ''}">Início</a>
            <a href="${ctx}/produtos" class="${noCatalogo and empty categoriaSelecionada and ordem != 'novidades' ? 'ativo' : ''}">Produtos</a>
            <c:forEach var="cat" items="${categoriasMenu}" end="2">
                <a href="${ctx}/produtos?categoria=${cat.idCategoria}"
                   class="${noCatalogo and categoriaSelecionada == cat.idCategoria ? 'ativo' : ''}"><c:out value="${cat.nome}" /></a>
            </c:forEach>
            <a href="${ctx}/produtos?ordem=novidades" class="${noCatalogo and empty categoriaSelecionada and ordem == 'novidades' ? 'ativo' : ''}">Novidades</a>
        </nav>

        <div class="icones">
            <button type="button" class="icone-botao" data-abrir-busca aria-controls="busca-topo" aria-expanded="false" aria-label="Buscar">
                <fs:icone nome="busca" />
            </button>

            <c:choose>
                <c:when test="${empty sessionScope.usuarioLogado}">
                    <a class="icone-botao" href="${ctx}/login" aria-label="Entrar na minha conta"><fs:icone nome="usuario" /></a>
                </c:when>
                <c:otherwise>
                    <div class="menu-conta">
                        <a class="icone-botao" href="${ctx}/perfil" aria-label="Minha conta" aria-haspopup="true"><fs:icone nome="usuario" /></a>
                        <div class="menu-conta-lista">
                            <div class="menu-conta-nome">Olá, <c:out value="${sessionScope.usuarioLogado.nome}" /></div>
                            <a href="${ctx}/pedidos">Meus pedidos</a>
                            <a href="${ctx}/perfil">Meu perfil</a>
                            <c:if test="${sessionScope.usuarioLogado.admin}">
                                <a href="${ctx}/admin">Painel administrativo</a>
                            </c:if>
                            <form class="form-inline" method="post" action="${ctx}/logout">
                                <button type="submit">Sair</button>
                            </form>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

            <c:set var="pecasCarrinho" value="${empty sessionScope.carrinho ? 0 : sessionScope.carrinho.quantidadeItens}" />
            <a class="icone-botao" href="${ctx}/carrinho" aria-label="Carrinho com ${pecasCarrinho} peça(s)">
                <fs:icone nome="sacola" />
                <span class="contador">${pecasCarrinho}</span>
            </a>
        </div>
    </div>

    <%-- Busca: abre ao clicar na lupa (sem JavaScript, o catálogo tem a própria busca) --%>
    <div id="busca-topo" class="busca-topo">
        <form class="container" method="get" action="${ctx}/produtos" role="search">
            <input class="campo" type="search" name="busca" maxlength="100" placeholder="O que você procura?" aria-label="Buscar produtos">
            <button type="submit" class="botao">Buscar</button>
        </form>
    </div>
</header>

<%-- Mensagem de uma vez só (ex.: "Produto adicionado ao carrinho"): mostra e apaga. --%>
<c:if test="${not empty sessionScope.mensagem}">
    <div class="container mensagem-flash">
        <div class="alerta alerta-info" role="status"><c:out value="${sessionScope.mensagem}" /></div>
    </div>
    <c:remove var="mensagem" scope="session" />
</c:if>
