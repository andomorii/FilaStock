package br.com.filastock.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.filastock.MainViewModel
import br.com.filastock.util.QR_PREFIX
import kotlinx.coroutines.launch

@Composable
fun AppNav(vm: MainViewModel) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(navController = nav, startDestination = "list") {
        composable("list") {
            ListScreen(
                vm = vm,
                onOpen = { nav.navigate("detail/$it") },
                onAdd = { nav.navigate("edit/0") },
                onScan = { nav.navigate("scan") },
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            DetailScreen(
                vm = vm,
                id = id,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("edit/$id") },
            )
        }

        composable(
            route = "edit/{id}?code={code}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("code") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            EditScreen(
                vm = vm,
                id = entry.arguments?.getLong("id") ?: 0L,
                scannedCode = entry.arguments?.getString("code"),
                onDone = { nav.popBackStack() },
            )
        }

        composable("scan") {
            ScanScreen(
                onBack = { nav.popBackStack() },
                onResult = { raw ->
                    scope.launch {
                        val code = raw.removePrefix(QR_PREFIX).trim()
                        if (code.isEmpty()) return@launch
                        val spool = vm.findByCode(code)
                        nav.popBackStack()
                        if (spool != null) {
                            nav.navigate("detail/${spool.id}")
                        } else {
                            nav.navigate("edit/0?code=${Uri.encode(code)}")
                        }
                    }
                },
            )
        }
    }
}
