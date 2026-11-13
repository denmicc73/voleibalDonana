/*
 * Pestañas de categoría del calendario (patrón "tabs" de WAI-ARIA).
 * Sin JavaScript se ven todas las listas, una debajo de otra.
 */
(function () {
    'use strict';

    document.querySelectorAll('[role="tablist"]').forEach(function (lista) {
        var pestanas = Array.prototype.slice.call(lista.querySelectorAll('[role="tab"]'));
        if (pestanas.length === 0) {
            return;
        }

        function seleccionar(elegida, enfocar) {
            pestanas.forEach(function (pestana) {
                var activa = pestana === elegida;
                pestana.setAttribute('aria-selected', String(activa));
                pestana.tabIndex = activa ? 0 : -1;
                var panel = document.getElementById(pestana.getAttribute('aria-controls'));
                if (panel) {
                    panel.hidden = !activa;
                }
            });
            if (enfocar) {
                elegida.focus();
            }
        }

        pestanas.forEach(function (pestana, indice) {
            pestana.addEventListener('click', function () {
                seleccionar(pestana, false);
            });
            pestana.addEventListener('keydown', function (evento) {
                var destino = null;
                if (evento.key === 'ArrowRight') {
                    destino = pestanas[(indice + 1) % pestanas.length];
                } else if (evento.key === 'ArrowLeft') {
                    destino = pestanas[(indice - 1 + pestanas.length) % pestanas.length];
                } else if (evento.key === 'Home') {
                    destino = pestanas[0];
                } else if (evento.key === 'End') {
                    destino = pestanas[pestanas.length - 1];
                }
                if (destino) {
                    evento.preventDefault();
                    seleccionar(destino, true);
                }
            });
        });

        var inicial = pestanas.filter(function (p) {
            return p.getAttribute('aria-selected') === 'true';
        })[0] || pestanas[0];
        seleccionar(inicial, false);
    });
})();
