<%-- Conteúdo comum do <head> de todas as páginas.
     Uso (dentro de <head>, depois do <title>):
         <%@ include file="/WEB-INF/views/includes/head.jsp" %>
     Páginas do admin definem antes: <c:set var="areaAdmin" value="true" />

     Fontes: Playfair Display (títulos, serifada) e Inter (textos), do Google Fonts.
     Sem internet, o navegador usa Georgia e Segoe UI/Arial, e o layout continua igual. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="pt_BR" scope="request" />
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="icon" type="image/png" href="${ctx}/assets/images/marca/monograma.png">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&family=Playfair+Display:wght@400;500&display=swap">
<link rel="stylesheet" href="${ctx}/assets/css/base.css">
<link rel="stylesheet" href="${ctx}/assets/css/layout.css">
<link rel="stylesheet" href="${ctx}/assets/css/componentes.css">
<link rel="stylesheet" href="${ctx}/assets/css/paginas.css">
<c:if test="${areaAdmin}"><link rel="stylesheet" href="${ctx}/assets/css/admin.css"></c:if>
<script src="${ctx}/assets/js/site.js" defer></script>
