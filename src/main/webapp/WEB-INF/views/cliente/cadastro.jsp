<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Formulário de cadastro.
     - ${param.xxx}: valor digitado (reaparece se houver erro);
     - ${erros.xxx}: mensagem de erro do campo (vinda do ClienteService).
     As senhas nunca são devolvidas para a página. --%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Criar conta - FashionStore</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/views/includes/cabecalho.jsp" %>

<main class="conteudo">
    <div class="cartao">
        <h2>Criar conta</h2>
        <p class="texto-suave">Campos com * são obrigatórios.</p>

        <c:if test="${not empty erroGeral}">
            <div class="alerta alerta-erro"><c:out value="${erroGeral}" /></div>
        </c:if>
        <c:if test="${not empty erros}">
            <div class="alerta alerta-erro">Corrija os campos destacados abaixo.</div>
        </c:if>

        <form method="post" action="${ctx}/cadastro" class="formulario" novalidate>

            <fieldset>
                <legend>Seus dados</legend>

                <label for="nome">Nome completo *</label>
                <input id="nome" name="nome" maxlength="100" required autocomplete="name"
                       value="<c:out value='${param.nome}' />" class="${not empty erros.nome ? 'campo-erro' : ''}">
                <c:if test="${not empty erros.nome}"><span class="msg-erro"><c:out value="${erros.nome}" /></span></c:if>

                <div class="linha">
                    <div>
                        <label for="email">E-mail *</label>
                        <input type="email" id="email" name="email" maxlength="150" required autocomplete="email"
                               value="<c:out value='${param.email}' />" class="${not empty erros.email ? 'campo-erro' : ''}">
                        <c:if test="${not empty erros.email}"><span class="msg-erro"><c:out value="${erros.email}" /></span></c:if>
                    </div>
                    <div>
                        <label for="cpf">CPF *</label>
                        <input id="cpf" name="cpf" maxlength="14" required inputmode="numeric" placeholder="000.000.000-00"
                               value="<c:out value='${param.cpf}' />" class="${not empty erros.cpf ? 'campo-erro' : ''}">
                        <c:if test="${not empty erros.cpf}"><span class="msg-erro"><c:out value="${erros.cpf}" /></span></c:if>
                    </div>
                </div>

                <label for="telefone">Telefone (com DDD)</label>
                <input id="telefone" name="telefone" maxlength="20" inputmode="tel" autocomplete="tel" placeholder="(11) 99999-9999"
                       value="<c:out value='${param.telefone}' />" class="${not empty erros.telefone ? 'campo-erro' : ''}">
                <c:if test="${not empty erros.telefone}"><span class="msg-erro"><c:out value="${erros.telefone}" /></span></c:if>

                <div class="linha">
                    <div>
                        <label for="senha">Senha *</label>
                        <input type="password" id="senha" name="senha" maxlength="72" required autocomplete="new-password"
                               class="${not empty erros.senha ? 'campo-erro' : ''}">
                        <span class="texto-suave">Mínimo 8 caracteres, com letras e números.</span>
                        <c:if test="${not empty erros.senha}"><span class="msg-erro"><c:out value="${erros.senha}" /></span></c:if>
                    </div>
                    <div>
                        <label for="confirmacao">Confirme a senha *</label>
                        <input type="password" id="confirmacao" name="confirmacao" maxlength="72" required autocomplete="new-password"
                               class="${not empty erros.confirmacao ? 'campo-erro' : ''}">
                        <c:if test="${not empty erros.confirmacao}"><span class="msg-erro"><c:out value="${erros.confirmacao}" /></span></c:if>
                    </div>
                </div>
            </fieldset>

            <fieldset>
                <legend>Endereço de entrega</legend>

                <div class="linha">
                    <div>
                        <label for="cep">CEP *</label>
                        <input id="cep" name="cep" maxlength="9" required inputmode="numeric" placeholder="00000-000" autocomplete="postal-code"
                               value="<c:out value='${param.cep}' />" class="${not empty erros.cep ? 'campo-erro' : ''}">
                        <c:if test="${not empty erros.cep}"><span class="msg-erro"><c:out value="${erros.cep}" /></span></c:if>
                    </div>
                    <div>
                        <label for="estado">Estado *</label>
                        <select id="estado" name="estado" required class="${not empty erros.estado ? 'campo-erro' : ''}">
                            <option value="">Selecione</option>
                            <c:forEach var="uf" items="AC,AL,AP,AM,BA,CE,DF,ES,GO,MA,MT,MS,MG,PA,PB,PR,PE,PI,RJ,RN,RS,RO,RR,SC,SP,SE,TO">
                                <option value="${uf}" ${param.estado == uf ? 'selected' : ''}>${uf}</option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty erros.estado}"><span class="msg-erro"><c:out value="${erros.estado}" /></span></c:if>
                    </div>
                </div>

                <label for="logradouro">Rua / Avenida *</label>
                <input id="logradouro" name="logradouro" maxlength="150" required autocomplete="address-line1"
                       value="<c:out value='${param.logradouro}' />" class="${not empty erros.logradouro ? 'campo-erro' : ''}">
                <c:if test="${not empty erros.logradouro}"><span class="msg-erro"><c:out value="${erros.logradouro}" /></span></c:if>

                <div class="linha">
                    <div>
                        <label for="numero">Número *</label>
                        <input id="numero" name="numero" maxlength="10" required
                               value="<c:out value='${param.numero}' />" class="${not empty erros.numero ? 'campo-erro' : ''}">
                        <c:if test="${not empty erros.numero}"><span class="msg-erro"><c:out value="${erros.numero}" /></span></c:if>
                    </div>
                    <div>
                        <label for="complemento">Complemento</label>
                        <input id="complemento" name="complemento" maxlength="100" autocomplete="address-line2"
                               value="<c:out value='${param.complemento}' />" class="${not empty erros.complemento ? 'campo-erro' : ''}">
                        <c:if test="${not empty erros.complemento}"><span class="msg-erro"><c:out value="${erros.complemento}" /></span></c:if>
                    </div>
                </div>

                <div class="linha">
                    <div>
                        <label for="bairro">Bairro *</label>
                        <input id="bairro" name="bairro" maxlength="100" required
                               value="<c:out value='${param.bairro}' />" class="${not empty erros.bairro ? 'campo-erro' : ''}">
                        <c:if test="${not empty erros.bairro}"><span class="msg-erro"><c:out value="${erros.bairro}" /></span></c:if>
                    </div>
                    <div>
                        <label for="cidade">Cidade *</label>
                        <input id="cidade" name="cidade" maxlength="100" required autocomplete="address-level2"
                               value="<c:out value='${param.cidade}' />" class="${not empty erros.cidade ? 'campo-erro' : ''}">
                        <c:if test="${not empty erros.cidade}"><span class="msg-erro"><c:out value="${erros.cidade}" /></span></c:if>
                    </div>
                </div>
            </fieldset>

            <button type="submit" class="botao botao-largo">Criar conta</button>
        </form>

        <p class="texto-centro">Já tem conta? <a href="${ctx}/login">Entrar</a></p>
    </div>
</main>

<%@ include file="/WEB-INF/views/includes/rodape.jsp" %>
</body>
</html>
