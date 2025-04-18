package com.example.fryzjer.data.network

import com.example.fryzjer.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalSettings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object SupabaseClient {

    // Domyślny (anon) klient
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.supabaseUrl,
        supabaseKey = BuildConfig.supabaseKey // <- Używa anon key
    ) {
        install(Auth) {
            flowType = FlowType.PKCE
        }
        install(Postgrest)
    }

    val auth = client.auth
    val supabase = client.postgrest

    // 👇 Klient admina (service_role key) – tylko do użytku na serwerze lub zaufanym kontekście
    val adminClient: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.supabaseUrl,
        supabaseKey = BuildConfig.service_key // <- Upewnij się, że dodałeś service_role key do BuildConfig!
    ) {
        install(Auth)
        install(Postgrest)
    }

    // Importuj token service_role do klienta admina w ramach coroutine
    init {
        GlobalScope.launch {
            try {
                // Here you import the service role token using `importAuthToken` method
                adminClient.auth.importAuthToken(BuildConfig.service_key)
            } catch (e: Exception) {
                // Handle any errors that might occur during the import
                println("Error importing auth token: ${e.localizedMessage}")
            }
        }
    }

    // Dostęp do admin.auth
    val adminAuthClient = adminClient.auth.admin // <-- Poprawny dostęp do admin.auth
}
