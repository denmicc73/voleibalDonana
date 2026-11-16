package es.voleibaldonana.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import es.voleibaldonana.dto.PoliticaPassword;
import es.voleibaldonana.exception.OperacionNoPermitidaException;
import es.voleibaldonana.model.Rol;
import es.voleibaldonana.service.UsuarioService;

/** Creación del primer administrador al arrancar. */
class AdminInicialTests {

    private final UsuarioService usuarioService = mock(UsuarioService.class);

    @Test
    void siYaHayAdministradorNoHaceNada() {
        when(usuarioService.hayAdministrador()).thenReturn(true);

        adminInicial(new AdminInicialProperties("a@b.es", "una frase bastante larga", "Admin"), "prod").run(null);

        verify(usuarioService, never()).crear(anyString(), anyString(), anyString(), any());
    }

    @Test
    void creaElAdministradorConLasVariables() {
        adminInicial(new AdminInicialProperties("a@b.es", "una frase bastante larga", "Admin"), "prod").run(null);

        verify(usuarioService).crear("Admin", "a@b.es", "una frase bastante larga", Rol.ADMIN);
    }

    @Test
    void passwordNoValidaImpideArrancar() {
        when(usuarioService.crear(anyString(), anyString(), anyString(), any()))
                .thenThrow(new OperacionNoPermitidaException(PoliticaPassword.MENSAJE));

        AdminInicial inicial = adminInicial(new AdminInicialProperties("a@b.es", "corta", "Admin"), "prod");

        assertThatThrownBy(() -> inicial.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_PASSWORD");
    }

    @Test
    void enDevSinVariablesCreaUnAdministradorDePrueba() {
        adminInicial(new AdminInicialProperties(null, null, "Admin"), "dev").run(null);

        verify(usuarioService).crear(anyString(), eq(AdminInicial.EMAIL_DEV), anyString(), eq(Rol.ADMIN));
    }

    @Test
    void enProdSinVariablesNoCreaNada() {
        adminInicial(new AdminInicialProperties("", "", "Admin"), "prod").run(null);

        verify(usuarioService, never()).crear(anyString(), anyString(), anyString(), any());
    }

    @Test
    void toStringNoMuestraLaPassword() {
        String texto = new AdminInicialProperties("a@b.es", "secreto muy secreto", "Admin").toString();

        org.assertj.core.api.Assertions.assertThat(texto).doesNotContain("secreto");
    }

    private AdminInicial adminInicial(AdminInicialProperties propiedades, String perfil) {
        MockEnvironment entorno = new MockEnvironment();
        entorno.setActiveProfiles(perfil);
        return new AdminInicial(usuarioService, propiedades, entorno);
    }
}
