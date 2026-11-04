package cl.aquaxor.app.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.aquaxor.app.AppContainer
import cl.aquaxor.app.ui.screens.AdminScreen
import cl.aquaxor.app.ui.screens.DetalleScreen
import cl.aquaxor.app.ui.screens.EvidenciaScreen
import cl.aquaxor.app.ui.screens.FormScreen
import cl.aquaxor.app.ui.screens.HistoryScreen
import cl.aquaxor.app.ui.screens.LoginScreen
import cl.aquaxor.app.ui.screens.MainScreen
import cl.aquaxor.app.ui.screens.StatusScreen
import cl.aquaxor.app.viewmodel.AdminViewModel
import cl.aquaxor.app.viewmodel.AquaViewModelFactory
import cl.aquaxor.app.viewmodel.SampleViewModel
import cl.aquaxor.app.viewmodel.SessionViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavGraph(container: AppContainer) {
    val factory = remember { AquaViewModelFactory(container) }
    val sessionVm: SessionViewModel = viewModel(factory = factory)
    val sampleVm: SampleViewModel = viewModel(factory = factory)
    val adminVm: AdminViewModel = viewModel(factory = factory)

    val nav = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    // Un único colector de mensajes: el Snackbar nunca se pierde al navegar.
    LaunchedEffect(Unit) {
        container.messages.mensajes.collectLatest { snackbarHostState.showSnackbar(it) }
    }

    fun navegarMenu(ruta: String) {
        if (ruta == Ruta.Form.ruta) sampleVm.nuevaMuestra()
        nav.navigate(ruta) {
            popUpTo(Ruta.Main.ruta)
            launchSingleTop = true
        }
    }

    fun cerrarSesion() {
        sampleVm.cancelar()
        sessionVm.logout()
        nav.navigate(Ruta.Login.ruta) {
            popUpTo(Ruta.Main.ruta) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = nav, startDestination = Ruta.Login.ruta) {

        composable(Ruta.Login.ruta) {
            LoginScreen(vm = sessionVm, snackbarHostState = snackbarHostState) {
                nav.navigate(Ruta.Main.ruta) {
                    popUpTo(Ruta.Login.ruta) { inclusive = true }
                }
            }
        }

        composable(Ruta.Main.ruta) {
            MainScreen(
                sessionVm = sessionVm,
                sampleVm = sampleVm,
                snackbarHostState = snackbarHostState,
                onNavegar = { navegarMenu(it) },
                onCerrarSesion = { cerrarSesion() }
            )
        }

        composable(Ruta.Form.ruta) {
            FormScreen(
                vm = sampleVm,
                snackbarHostState = snackbarHostState,
                onBack = {
                    sampleVm.cancelar()
                    nav.popBackStack()
                },
                onSiguiente = { nav.navigate(Ruta.Evidencia.ruta) }
            )
        }

        composable(Ruta.Evidencia.ruta) {
            EvidenciaScreen(
                vm = sampleVm,
                snackbarHostState = snackbarHostState,
                onBack = { nav.popBackStack() },
                onGuardada = { id ->
                    nav.navigate(Ruta.Detalle.crear(id)) {
                        popUpTo(Ruta.Form.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Ruta.Historial.ruta) {
            HistoryScreen(
                sessionVm = sessionVm,
                sampleVm = sampleVm,
                snackbarHostState = snackbarHostState,
                onNavegar = { navegarMenu(it) },
                onCerrarSesion = { cerrarSesion() },
                onAbrirMuestra = { nav.navigate(Ruta.Detalle.crear(it)) }
            )
        }

        composable(Ruta.Estados.ruta) {
            StatusScreen(
                sessionVm = sessionVm,
                sampleVm = sampleVm,
                snackbarHostState = snackbarHostState,
                onNavegar = { navegarMenu(it) },
                onCerrarSesion = { cerrarSesion() },
                onAbrirMuestra = { nav.navigate(Ruta.Detalle.crear(it)) }
            )
        }

        composable(Ruta.Admin.ruta) {
            AdminScreen(
                sessionVm = sessionVm,
                adminVm = adminVm,
                snackbarHostState = snackbarHostState,
                onNavegar = { navegarMenu(it) },
                onCerrarSesion = { cerrarSesion() }
            )
        }

        composable(
            route = Ruta.Detalle.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            DetalleScreen(
                id = id,
                sessionVm = sessionVm,
                sampleVm = sampleVm,
                snackbarHostState = snackbarHostState,
                onBack = { nav.popBackStack() },
                onEditar = { sampleVm.cargarParaEditar(id) { nav.navigate(Ruta.Form.ruta) } },
                onEliminada = { nav.popBackStack() }
            )
        }
    }
}
