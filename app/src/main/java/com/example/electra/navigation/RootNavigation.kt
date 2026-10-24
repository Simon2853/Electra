package com.example.electra.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.electra.data.AppContainer
import com.example.electra.ui.viewmodel.CrearProtocoloViewModel
import com.example.electra.ui.viewmodel.DetalleProtocoloViewModel
import com.example.electra.ui.viewmodel.DetallePruebaViewModel
import com.example.electra.ui.viewmodel.HistorialEquipoViewModel
import com.example.electra.ui.viewmodel.LoginViewModel
import com.example.electra.ui.viewmodel.ProtocolosViewModel
import com.example.electra.ui.viewmodel.RegistrarPruebaViewModel
import com.example.electra.ui.viewmodel.RegistroViewModel
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

@Composable
fun RootNavigation(contenedor: AppContainer) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // INICIO DE SESIÓN
        composable(Routes.LOGIN) {
            Login(
                viewModel = viewModel { LoginViewModel(contenedor.usuarios) },
                onLoginClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onRegistroClick = {
                    navController.navigate(Routes.REGISTRO)
                }
            )
        }

        // REGISTRO DE USUARIO
        composable(Routes.REGISTRO) {
            Registro(
                viewModel = viewModel { RegistroViewModel(contenedor.usuarios) },
                onVolverClick = {
                    navController.popBackStack()
                },
                onRegistroExitoso = {
                    navController.popBackStack()
                }
            )
        }

        // HOME
        composable(Routes.HOME) {
            Home(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        // LISTA DE EQUIPOS
        composable(Routes.EQUIPOS) {
            Equipos(
                onEquipoClick = { codigo ->
                    navController.navigate(
                        Routes.DETALLE_EQUIPO_PARAM.replace(
                            "{codigo}",
                            codigo
                        )
                    )
                }
            )
        }

        // DETALLE DEL EQUIPO
        composable(
            route = Routes.DETALLE_EQUIPO_PARAM,
            arguments = listOf(
                navArgument("codigo") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val codigoEquipo =
                backStackEntry.arguments?.getString("codigo") ?: "TR-001"

            DetalleEquipo(
                codigoEquipo = codigoEquipo,
                onVolverClick = {
                    navController.popBackStack()
                },
                onNuevaPruebaClick = {
                    navController.navigate(
                        Routes.REGISTRAR_PRUEBA_PARAM.replace(
                            "{codigo}",
                            codigoEquipo
                        )
                    )
                },
                onHistorialClick = {
                    navController.navigate(
                        Routes.HISTORIAL_EQUIPO_PARAM.replace(
                            "{codigo}",
                            codigoEquipo
                        )
                    )
                },
                onProtocolosClick = {
                    navController.navigate(Routes.PROTOCOLOS)
                }
            )
        }

        // CREAR PROTOCOLO
        composable(Routes.CREAR_PROTOCOLO) {
            CrearProtocolo(
                viewModel = viewModel { CrearProtocoloViewModel(contenedor.protocolos, contenedor.pruebas) },
                onBackClick = {
                    navController.popBackStack()
                },
                onProtocoloGuardado = {
                    navController.popBackStack()
                }
            )
        }

        // REGISTRAR PRUEBA
        composable(
            route = Routes.REGISTRAR_PRUEBA_PARAM,
            arguments = listOf(
                navArgument("codigo") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val codigoEquipo =
                backStackEntry.arguments?.getString("codigo") ?: "TR-001"

            RegistrarPrueba(
                viewModel = viewModel { RegistrarPruebaViewModel(contenedor.pruebas) },
                codigoEquipo = codigoEquipo,
                onVolverClick = {
                    navController.popBackStack()
                }
            )
        }

        // CONFIRMACIÓN DE PRUEBA
        composable(Routes.CONFIRMACION_PRUEBA) {
            ConfirmacionPrueba()
        }

        // LISTA DE PROTOCOLOS
        composable(Routes.PROTOCOLOS) {
            Protocolos(
                viewModel = viewModel { ProtocolosViewModel(contenedor.protocolos) },
                onBackClick = { navController.popBackStack() },
                onProtocolClick = { numeroProtocolo ->
                    navController.navigate(Routes.protocolo(numeroProtocolo))
                },
                onCrearProtocoloClick = {
                    navController.navigate(Routes.CREAR_PROTOCOLO)
                }
            )
        }

        // DETALLE DE UN PROTOCOLO (CORREGIDO)
        composable(
            route = Routes.PROTOCOLO_PARAM,
            arguments = listOf(navArgument("numero") { type = NavType.StringType })
        ) { backStackEntry ->
            val numeroProtocolo = backStackEntry.arguments?.getString("numero") ?: "000125"

            DetalleProtocolo(
                viewModel = viewModel { DetalleProtocoloViewModel(contenedor.protocolos, numeroProtocolo) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // HISTORIAL DEL EQUIPO
        composable(
            route = Routes.HISTORIAL_EQUIPO_PARAM,
            arguments = listOf(navArgument("codigo") { type = NavType.StringType })
        ) { backStackEntry ->
            val codigoEquipo = backStackEntry.arguments?.getString("codigo") ?: "TR-001"
            HistorialEquipo(
                viewModel = viewModel { HistorialEquipoViewModel(contenedor.pruebas, codigoEquipo) },
                onBackClick = { navController.popBackStack() },
                onPruebaClick = { idPrueba ->
                    navController.navigate(Routes.detallePrueba(idPrueba))
                }
            )
        }

        // DETALLE DE PRUEBA
        composable(
            route = Routes.DETALLE_PRUEBA_PARAM,
            arguments = listOf(navArgument("idPrueba") { type = NavType.LongType })
        ) { backStackEntry ->
            val idPrueba = backStackEntry.arguments?.getLong("idPrueba") ?: 1L
            DetallePrueba(
                idPrueba = idPrueba,
                viewModel = viewModel { DetallePruebaViewModel(contenedor.pruebas, idPrueba) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // PERFIL
        composable(Routes.PERFIL) {
            Perfil(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onSyncClick = {
                    // Implementar sincronización más adelante.
                }
            )
        }
    }
}