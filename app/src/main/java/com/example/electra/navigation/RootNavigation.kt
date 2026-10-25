package com.example.electra.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.electra.data.AppContainer
import com.example.electra.ui.screen.ConfirmacionPrueba
import com.example.electra.ui.screen.CrearProtocolo
import com.example.electra.ui.screen.DetalleEquipo
import com.example.electra.ui.screen.DetalleProtocolo
import com.example.electra.ui.screen.DetallePrueba
import com.example.electra.ui.screen.Equipos
import com.example.electra.ui.screen.HistorialEquipo
import com.example.electra.ui.screen.Home
import com.example.electra.ui.screen.Login
import com.example.electra.ui.screen.Perfil
import com.example.electra.ui.screen.Protocolos
import com.example.electra.ui.screen.RegistrarPrueba
import com.example.electra.ui.screen.Registro
import com.example.electra.ui.viewmodel.CrearProtocoloViewModel
import com.example.electra.ui.viewmodel.DetalleProtocoloViewModel
import com.example.electra.ui.viewmodel.DetallePruebaViewModel
import com.example.electra.ui.viewmodel.HistorialEquipoViewModel
import com.example.electra.ui.viewmodel.HomeViewModel
import com.example.electra.ui.viewmodel.LoginViewModel
import com.example.electra.ui.viewmodel.PerfilViewModel
import com.example.electra.ui.viewmodel.ProtocolosViewModel
import com.example.electra.ui.viewmodel.RegistrarPruebaViewModel
import com.example.electra.ui.viewmodel.RegistroViewModel

private const val DURACION_TRANSICION = 350

@Composable
fun RootNavigation(contenedor: AppContainer) {
    val navController = rememberNavController()

    val inicio = remember { if (contenedor.sesion.usuarioActual() != null) Routes.HOME else Routes.LOGIN }

    NavHost(
        navController = navController,
        startDestination = inicio,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(DURACION_TRANSICION)) +
                    fadeIn(tween(DURACION_TRANSICION))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(DURACION_TRANSICION)) { it / 4 } +
                    fadeOut(tween(DURACION_TRANSICION))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(DURACION_TRANSICION)) { it / 4 } +
                    fadeIn(tween(DURACION_TRANSICION))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(DURACION_TRANSICION)) +
                    fadeOut(tween(DURACION_TRANSICION))
        }
    ) {
        composable(Routes.LOGIN) {
            Login(
                viewModel = viewModel { LoginViewModel(contenedor.usuarios, contenedor.sesion) },
                onLoginClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegistroClick = { navController.navigate(Routes.REGISTRO) }
            )
        }

        composable(Routes.REGISTRO) {
            Registro(
                viewModel = viewModel { RegistroViewModel(contenedor.usuarios) },
                onVolverClick = { navController.popBackStack() },
                onRegistroExitoso = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            Home(
                viewModel = viewModel {
                    HomeViewModel(contenedor.pruebas, contenedor.protocolos, contenedor.usuarios, contenedor.sesion)
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Routes.EQUIPOS) {
            Equipos(
                onEquipoClick = { codigo -> navController.navigate(Routes.detalleEquipo(codigo)) },
                onVolverClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DETALLE_EQUIPO_PARAM,
            arguments = listOf(navArgument("codigo") { type = NavType.StringType })
        ) { backStackEntry ->
            val codigoEquipo = backStackEntry.arguments?.getString("codigo").orEmpty()

            DetalleEquipo(
                codigoEquipo = codigoEquipo,
                onVolverClick = { navController.popBackStack() },
                onNuevaPruebaClick = { navController.navigate(Routes.registrarPrueba(codigoEquipo)) },
                onHistorialClick = { navController.navigate(Routes.historialEquipo(codigoEquipo)) },
                onProtocolosClick = { navController.navigate(Routes.PROTOCOLOS) }
            )
        }

        composable(
            route = Routes.REGISTRAR_PRUEBA_PARAM,
            arguments = listOf(navArgument("codigo") { type = NavType.StringType })
        ) { backStackEntry ->
            val codigoEquipo = backStackEntry.arguments?.getString("codigo").orEmpty()

            RegistrarPrueba(
                viewModel = viewModel { RegistrarPruebaViewModel(contenedor.pruebas, contenedor.ubicacion, codigoEquipo) },
                onVolverClick = { navController.popBackStack() },
                onPruebaGuardada = { id ->

                    navController.navigate(Routes.confirmacionPrueba(id)) {
                        popUpTo(Routes.REGISTRAR_PRUEBA_PARAM) { inclusive = true }
                    }
                }
            )
        }


        composable(
            route = Routes.CONFIRMACION_PRUEBA_PARAM,
            arguments = listOf(navArgument("idPrueba") { type = NavType.LongType })
        ) { backStackEntry ->
            val idPrueba = backStackEntry.arguments?.getLong("idPrueba") ?: 0L

            ConfirmacionPrueba(
                viewModel = viewModel { DetallePruebaViewModel(contenedor.pruebas, idPrueba) },
                onVerHistorial = { codigo ->
                    navController.navigate(Routes.historialEquipo(codigo)) {
                        popUpTo(Routes.CONFIRMACION_PRUEBA_PARAM) { inclusive = true }
                    }
                },
                onInicio = { navController.volverAlInicio() }
            )
        }


        composable(Routes.PROTOCOLOS) {
            Protocolos(
                viewModel = viewModel { ProtocolosViewModel(contenedor.protocolos) },
                onBackClick = { navController.popBackStack() },
                onProtocolClick = { numero -> navController.navigate(Routes.protocolo(numero)) },
                onCrearProtocoloClick = { navController.navigate(Routes.CREAR_PROTOCOLO) }
            )
        }


        composable(Routes.CREAR_PROTOCOLO) {
            CrearProtocolo(
                viewModel = viewModel { CrearProtocoloViewModel(contenedor.protocolos, contenedor.pruebas) },
                onBackClick = { navController.popBackStack() },
                onProtocoloGuardado = { navController.popBackStack() }
            )
        }


        composable(
            route = Routes.PROTOCOLO_PARAM,
            arguments = listOf(navArgument("numero") { type = NavType.StringType })
        ) { backStackEntry ->
            val numeroProtocolo = backStackEntry.arguments?.getString("numero").orEmpty()

            DetalleProtocolo(
                viewModel = viewModel {
                    DetalleProtocoloViewModel(contenedor.protocolos, contenedor.pruebas, numeroProtocolo)
                },
                onBackClick = { navController.popBackStack() },
                onPruebaClick = { id -> navController.navigate(Routes.detallePrueba(id)) }
            )
        }


        composable(
            route = Routes.HISTORIAL_EQUIPO_PARAM,
            arguments = listOf(navArgument("codigo") { type = NavType.StringType })
        ) { backStackEntry ->
            val codigoEquipo = backStackEntry.arguments?.getString("codigo").orEmpty()

            HistorialEquipo(
                viewModel = viewModel { HistorialEquipoViewModel(contenedor.pruebas, codigoEquipo) },
                onBackClick = { navController.popBackStack() },
                onPruebaClick = { idPrueba -> navController.navigate(Routes.detallePrueba(idPrueba)) }
            )
        }


        composable(
            route = Routes.DETALLE_PRUEBA_PARAM,
            arguments = listOf(navArgument("idPrueba") { type = NavType.LongType })
        ) { backStackEntry ->
            val idPrueba = backStackEntry.arguments?.getLong("idPrueba") ?: 0L

            DetallePrueba(
                idPrueba = idPrueba,
                viewModel = viewModel { DetallePruebaViewModel(contenedor.pruebas, idPrueba) },
                onBackClick = { navController.popBackStack() }
            )
        }


        composable(Routes.PERFIL) {
            Perfil(
                viewModel = viewModel {
                    PerfilViewModel(contenedor.usuarios, contenedor.sesion, contenedor.pruebas, contenedor.protocolos)
                },
                onBackClick = { navController.popBackStack() },
                onSesionCerrada = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}


private fun NavHostController.volverAlInicio() {
    if (!popBackStack(Routes.HOME, inclusive = false)) {
        navigate(Routes.HOME) { launchSingleTop = true }
    }
}
