<%-- Tag reutilizável: mostra a imagem de um produto.
     Uso: <%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
          <fs:imagemProduto produto="${p}" classe="card-imagem" />

     Regras:
     - imagem vazia            -> imagem padrão (sem-imagem.svg)
     - começa com http(s)://   -> usa o endereço como está
     - nome de arquivo         -> procura em assets/images/produtos/
     - arquivo não encontrado  -> o navegador troca pela imagem padrão (onerror)

     "Tag file" é um recurso padrão do JSP para não repetir o mesmo HTML
     em várias páginas (catálogo, detalhes, admin, carrinho...). --%>
<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="produto" required="true" type="br.com.fashionstore.model.Produto" %>
<%@ attribute name="classe" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="padrao" value="${ctx}/assets/images/sem-imagem.svg" />
<c:choose>
    <c:when test="${empty produto.imagem}">
        <c:set var="url" value="${padrao}" />
    </c:when>
    <c:when test="${fn:startsWith(produto.imagem, 'http://') or fn:startsWith(produto.imagem, 'https://')}">
        <c:set var="url" value="${produto.imagem}" />
    </c:when>
    <c:otherwise>
        <c:set var="url" value="${ctx}/assets/images/produtos/${produto.imagem}" />
    </c:otherwise>
</c:choose>
<img src="<c:out value='${url}' />" alt="<c:out value='${produto.nome}' />" class="<c:out value='${classe}' />"
     loading="lazy" onerror="this.onerror=null;this.src='${padrao}';">
