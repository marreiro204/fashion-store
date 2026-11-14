<%-- Ícones de linha (SVG), no estilo da referência.
     Uso: <fs:icone nome="sacola" />
     Nomes: busca, usuario, sacola, anterior, proximo. --%>
<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="nome" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
<c:choose>
    <c:when test="${nome == 'busca'}"><circle cx="10.5" cy="10.5" r="6.5"/><path d="M15.5 15.5 21 21"/></c:when>
    <c:when test="${nome == 'usuario'}"><circle cx="12" cy="8" r="4"/><path d="M4.5 21c.8-4 3.8-6.5 7.5-6.5s6.7 2.5 7.5 6.5"/></c:when>
    <c:when test="${nome == 'sacola'}"><path d="M3 4h2.2l2.3 11.2a1.5 1.5 0 0 0 1.5 1.2h8.6a1.5 1.5 0 0 0 1.5-1.2L20.8 8H6.1"/><circle cx="9.5" cy="20" r="1.2"/><circle cx="17" cy="20" r="1.2"/></c:when>
    <c:when test="${nome == 'anterior'}"><path d="M15 5 8 12l7 7"/></c:when>
    <c:when test="${nome == 'proximo'}"><path d="m9 5 7 7-7 7"/></c:when>
</c:choose>
</svg>
