<%-- Card de produto (vitrine, catálogo, "você também pode gostar").
     Uso: <fs:cardProduto produto="${p}" />
     A imagem fica numa moldura 3:4 e é recortada sem deformar. --%>
<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="produto" required="true" type="br.com.fashionstore.model.Produto" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fs" tagdir="/WEB-INF/tags" %>
<a class="card-produto ${produto.esgotado ? 'esgotado' : ''}" href="${pageContext.request.contextPath}/produto?id=${produto.idProduto}">
    <div class="card-produto-imagem">
        <fs:imagemProduto produto="${produto}" />
        <c:if test="${produto.esgotado}"><span class="selo selo-escuro card-produto-selo">Esgotado</span></c:if>
    </div>
    <span class="card-produto-categoria"><c:out value="${produto.categoria.nome}" /></span>
    <span class="card-produto-nome"><c:out value="${produto.nome}" /></span>
    <span class="card-produto-preco"><fmt:formatNumber value="${produto.preco}" type="currency" /></span>
</a>
