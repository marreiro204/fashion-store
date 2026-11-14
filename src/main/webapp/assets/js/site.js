/* =====================================================================
   FashionStore - comportamentos da interface.

   Tudo aqui é "melhoria": se o JavaScript estiver desligado, o site
   continua funcionando (formulários, links e botões são HTML normal).
   ===================================================================== */
(function () {
    "use strict";

    /* ---------- Barra de aviso: alterna as mensagens ---------- */
    document.querySelectorAll("[data-aviso]").forEach(function (barra) {
        var mensagens = barra.querySelectorAll(".aviso-mensagem");
        if (mensagens.length < 2) { return; }
        var atual = 0;
        function mostrar(i) {
            mensagens[atual].hidden = true;
            atual = (i + mensagens.length) % mensagens.length;
            mensagens[atual].hidden = false;
        }
        barra.querySelector("[data-aviso-anterior]").addEventListener("click", function () { mostrar(atual - 1); });
        barra.querySelector("[data-aviso-proximo]").addEventListener("click", function () { mostrar(atual + 1); });
        setInterval(function () { mostrar(atual + 1); }, 6000);
    });

    /* ---------- Banner principal: carrossel ---------- */
    document.querySelectorAll("[data-carrossel]").forEach(function (carrossel) {
        var slides = carrossel.querySelectorAll(".banner-slide");
        var pontos = carrossel.querySelectorAll(".banner-ponto");
        if (slides.length < 2) { return; }
        var atual = 0, timer;
        function ir(i) {
            slides[atual].classList.remove("ativo");
            pontos[atual].classList.remove("ativo");
            atual = (i + slides.length) % slides.length;
            slides[atual].classList.add("ativo");
            pontos[atual].classList.add("ativo");
        }
        function reiniciar() { clearInterval(timer); timer = setInterval(function () { ir(atual + 1); }, 7000); }
        pontos.forEach(function (ponto, i) { ponto.addEventListener("click", function () { ir(i); reiniciar(); }); });
        reiniciar();
    });

    /* ---------- Busca do cabeçalho ---------- */
    var botaoBusca = document.querySelector("[data-abrir-busca]");
    var busca = document.getElementById("busca-topo");
    if (botaoBusca && busca) {
        botaoBusca.addEventListener("click", function () {
            var abrir = !busca.classList.contains("aberta");
            busca.classList.toggle("aberta", abrir);
            botaoBusca.setAttribute("aria-expanded", abrir);
            if (abrir) { busca.querySelector("input").focus(); }
        });
    }

    /* ---------- Galeria do produto: troca a imagem principal ---------- */
    document.querySelectorAll("[data-galeria]").forEach(function (galeria) {
        var principal = galeria.querySelector(".galeria-principal img");
        galeria.querySelectorAll(".galeria-miniatura").forEach(function (miniatura) {
            miniatura.addEventListener("click", function () {
                principal.src = miniatura.dataset.imagem;
                galeria.querySelectorAll(".galeria-miniatura").forEach(function (m) { m.classList.remove("ativa"); });
                miniatura.classList.add("ativa");
            });
        });
    });

    /* ---------- Quantidade: botões - e + ---------- */
    document.querySelectorAll("[data-quantidade]").forEach(function (caixa) {
        var campo = caixa.querySelector("input");
        var min = parseInt(campo.min || "0", 10), max = parseInt(campo.max || "99", 10);
        caixa.querySelectorAll("button[data-passo]").forEach(function (botao) {
            botao.addEventListener("click", function () {
                var valor = (parseInt(campo.value, 10) || 0) + parseInt(botao.dataset.passo, 10);
                campo.value = Math.min(max, Math.max(min, valor));
            });
        });
    });

    /* ---------- Confirmação antes de ações importantes ---------- */
    document.querySelectorAll("form[data-confirmar]").forEach(function (form) {
        form.addEventListener("submit", function (evento) {
            var condicao = form.dataset.confirmarSe;     // ex.: só confirmar se status=CANCELADO
            if (condicao) {
                var partes = condicao.split("=");
                var campo = form.elements[partes[0]];
                if (!campo || campo.value !== partes[1]) { return; }
            }
            if (!window.confirm(form.dataset.confirmar)) { evento.preventDefault(); }
        });
    });
})();
