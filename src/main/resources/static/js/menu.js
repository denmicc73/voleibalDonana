/* Menú móvil: el botón hamburguesa abre y cierra la navegación principal. */
(function () {
    'use strict';

    var boton = document.querySelector('.boton-menu');
    if (!boton) {
        return;
    }
    var menu = document.getElementById(boton.getAttribute('aria-controls'));
    if (!menu) {
        return;
    }

    function cambiar(abrir) {
        boton.setAttribute('aria-expanded', String(abrir));
        menu.classList.toggle('menu--abierto', abrir);
    }

    boton.addEventListener('click', function () {
        cambiar(boton.getAttribute('aria-expanded') !== 'true');
    });

    // Al elegir un enlace (anclas de la portada) se cierra el menú.
    menu.addEventListener('click', function (evento) {
        if (evento.target.closest('a')) {
            cambiar(false);
        }
    });

    // Escape cierra el menú y devuelve el foco al botón.
    document.addEventListener('keydown', function (evento) {
        if (evento.key === 'Escape' && boton.getAttribute('aria-expanded') === 'true') {
            cambiar(false);
            boton.focus();
        }
    });
})();
