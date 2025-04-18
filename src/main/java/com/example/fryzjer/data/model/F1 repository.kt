package com.example.fryzjer.data.model

import android.util.Log
import com.example.fryzjer.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.Serializable

// Klasy danych dla wyścigów
@Serializable
data class Race(
    val race_id: String,
    val race_name: String,
    val track_id: String,
    val race_date: String, // Używamy String dla daty, aby uprościć serializację
    val laps: Int,
    val winner_driver_id: String? = null
)

@Serializable
data class RaceInput(
    val race_name: String,
    val track_id: String,
    val race_date: String,
    val laps: Int,
    val winner_driver_id: String? = null
)

// Klasy danych dla torów
@Serializable
data class Track(
    val track_id: String,
    val track_name: String,
    val location: String,
    val length: Float,
    val lap_record: String?
)

@Serializable
data class TrackInput(
    val track_name: String,
    val location: String,
    val length: Float,
    val lap_record: String? = null
)

// Klasy danych dla zespołów
@Serializable
data class Team(
    val team_id: String,
    val team_name: String,
    val nationality: String?,
    val founded_year: Int?
)

@Serializable
data class TeamInput(
    val team_name: String,
    val nationality: String? = null,
    val founded_year: Int? = null
)

// Klasy danych dla kierowców
@Serializable
data class Driver(
    val driver_id: String,
    val first_name: String,
    val last_name: String,
    val nationality: String?,
    val date_of_birth: String?, // Używamy String dla daty
    val team_id: String?
)

@Serializable
data class DriverInput(
    val first_name: String,
    val last_name: String,
    val nationality: String? = null,
    val date_of_birth: String? = null,
    val team_id: String? = null
)

@Serializable
data class RaceResult(
    val result_id: String,
    val race_id: String,
    val driver_id: String,
    val position: Int,
    val points: Int
)

@Serializable
data class RaceResultInput(
    val race_id: String,
    val driver_id: String,
    val position: Int,
    val points: Int
)
@Serializable
data class Article(
    val article_id: String,
    val title: String,
    val content: String,
    val created_at: String,
    val updated_at: String?,
    val author_id: String?
)

@Serializable
data class ArticleInput(
    val title: String,
    val content: String,
    val author_id: String? = null
)

object F1Repository {

    // Operacje na wyścigach

    suspend fun createRace(race: RaceInput): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Races")
                .insert(race)
        } catch (e: Exception) {
            Log.e("F1Repository", "Error creating race", e)
            throw e
        }
    }

    suspend fun getAllRaces(): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Races")
                .select()
        } catch (e: Exception) {
            Log.e("F1Repository", "Error fetching races", e)
            throw e
        }
    }

    suspend fun updateRace(raceId: String, race: RaceInput) {
        try {
            SupabaseClient.supabase
                .from("Races")
                .update(race) {
                    filter {
                        eq("race_id", raceId)
                    }
                }
            Log.d("F1Repository", "Successfully updated race $raceId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error updating race $raceId", e)
            throw e
        }
    }

    suspend fun deleteRace(raceId: String) {
        try {
            SupabaseClient.supabase
                .from("Races")
                .delete {
                    filter {
                        eq("race_id", raceId)
                    }
                }
            Log.d("F1Repository", "Successfully deleted race $raceId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error deleting race $raceId", e)
            throw e
        }
    }

    // Operacje na torach

    suspend fun createTrack(track: TrackInput): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Tracks")
                .insert(track)
        } catch (e: Exception) {
            Log.e("F1Repository", "Error creating track", e)
            throw e
        }
    }

    suspend fun getAllTracks(): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Tracks")
                .select()
        } catch (e: Exception) {
            Log.e("F1Repository", "Error fetching tracks", e)
            throw e
        }
    }

    suspend fun updateTrack(trackId: String, track: TrackInput) {
        try {
            SupabaseClient.supabase
                .from("Tracks")
                .update(track) {
                    filter {
                        eq("track_id", trackId)
                    }
                }
            Log.d("F1Repository", "Successfully updated track $trackId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error updating track $trackId", e)
            throw e
        }
    }

    suspend fun deleteTrack(trackId: String) {
        try {
            SupabaseClient.supabase
                .from("Tracks")
                .delete {
                    filter {
                        eq("track_id", trackId)
                    }
                }
            Log.d("F1Repository", "Successfully deleted track $trackId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error deleting track $trackId", e)
            throw e
        }
    }

    // Operacje na zespołach

    suspend fun createTeam(team: TeamInput): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Teams")
                .insert(team)
        } catch (e: Exception) {
            Log.e("F1Repository", "Error creating team", e)
            throw e
        }
    }

    suspend fun getAllTeams(): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Teams")
                .select()
        } catch (e: Exception) {
            Log.e("F1Repository", "Error fetching teams", e)
            throw e
        }
    }

    suspend fun updateTeam(teamId: String, team: TeamInput) {
        try {
            SupabaseClient.supabase
                .from("Teams")
                .update(team) {
                    filter {
                        eq("team_id", teamId)
                    }
                }
            Log.d("F1Repository", "Successfully updated team $teamId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error updating team $teamId", e)
            throw e
        }
    }

    suspend fun deleteTeam(teamId: String) {
        try {
            SupabaseClient.supabase
                .from("Teams")
                .delete {
                    filter {
                        eq("team_id", teamId)
                    }
                }
            Log.d("F1Repository", "Successfully deleted team $teamId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error deleting team $teamId", e)
            throw e
        }
    }

    // Operacje na kierowcach

    suspend fun createDriver(driver: DriverInput): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Drivers")
                .insert(driver)
        } catch (e: Exception) {
            Log.e("F1Repository", "Error creating driver", e)
            throw e
        }
    }

    suspend fun getAllDrivers(): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Drivers")
                .select()
        } catch (e: Exception) {
            Log.e("F1Repository", "Error fetching drivers", e)
            throw e
        }
    }

    suspend fun updateDriver(driverId: String, driver: DriverInput) {
        try {
            SupabaseClient.supabase
                .from("Drivers")
                .update(driver) {
                    filter {
                        eq("driver_id", driverId)
                    }
                }
            Log.d("F1Repository", "Successfully updated driver $driverId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error updating driver $driverId", e)
            throw e
        }
    }

    suspend fun deleteDriver(driverId: String) {
        try {
            SupabaseClient.supabase
                .from("Drivers")
                .delete {
                    filter {
                        eq("driver_id", driverId)
                    }
                }
            Log.d("F1Repository", "Successfully deleted driver $driverId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error deleting driver $driverId", e)
            throw e
        }
    }
    // Operacje na wynikach wyścigów w F1Repository
    suspend fun createRaceResult(result: RaceResultInput): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("RaceResults")
                .insert(result)
        } catch (e: Exception) {
            Log.e("F1Repository", "Error creating race result", e)
            throw e
        }
    }

    suspend fun getAllRaceResults(): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("RaceResults")
                .select()
        } catch (e: Exception) {
            Log.e("F1Repository", "Error fetching race results", e)
            throw e
        }
    }

    suspend fun updateRaceResult(resultId: String, result: RaceResultInput) {
        try {
            SupabaseClient.supabase
                .from("RaceResults")
                .update(result) {
                    filter {
                        eq("result_id", resultId)
                    }
                }
            Log.d("F1Repository", "Successfully updated race result $resultId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error updating race result $resultId", e)
            throw e
        }
    }

    suspend fun deleteRaceResult(resultId: String) {
        try {
            SupabaseClient.supabase
                .from("RaceResults")
                .delete {
                    filter {
                        eq("result_id", resultId)
                    }
                }
            Log.d("F1Repository", "Successfully deleted race result $resultId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error deleting race result $resultId", e)
            throw e
        }
    }
    // Dodaj te metody do obiektu F1Repository
    suspend fun createArticle(article: ArticleInput): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Articles")
                .insert(article)
        } catch (e: Exception) {
            Log.e("F1Repository", "Error creating article", e)
            throw e
        }
    }

    suspend fun getAllArticles(): PostgrestResult {
        return try {
            SupabaseClient.supabase
                .from("Articles")
                .select()
        } catch (e: Exception) {
            Log.e("F1Repository", "Error fetching articles", e)
            throw e
        }
    }

    suspend fun updateArticle(articleId: String, article: ArticleInput) {
        try {
            SupabaseClient.supabase
                .from("Articles")
                .update(article) {
                    filter {
                        eq("article_id", articleId)
                    }
                }
            Log.d("F1Repository", "Successfully updated article $articleId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error updating article $articleId", e)
            throw e
        }
    }

    suspend fun deleteArticle(articleId: String) {
        try {
            SupabaseClient.supabase
                .from("Articles")
                .delete {
                    filter {
                        eq("article_id", articleId)
                    }
                }
            Log.d("F1Repository", "Successfully deleted article $articleId")
        } catch (e: Exception) {
            Log.e("F1Repository", "Error deleting article $articleId", e)
            throw e
        }
    }
    suspend fun getCurrentUserId(): String? {
        return try {
            val user = SupabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            user.id
        } catch (e: Exception) {
            Log.e("F1Repository", "Error getting current user ID", e)
            null
        }
    }
    suspend fun getArticleById(articleId: String): Article? {
        return try {
            val result = SupabaseClient.supabase
                .from("Articles")
                .select {
                    filter {
                        eq("article_id", articleId)
                    }
                }

            result.decodeList<Article>().firstOrNull()
        } catch (e: Exception) {
            Log.e("F1Repository", "Error fetching article $articleId", e)
            null
        }
    }
    suspend fun getAuthorsInfo(authorIds: List<String>): Map<String, String> {
        return try {
            val result = mutableMapOf<String, String>()

            // Use the admin auth client to retrieve users
            val users = SupabaseClient.adminAuthClient.retrieveUsers()

            // Filter users by the provided authorIds
            users.forEach { user ->
                val userId = user.id
                if (authorIds.contains(userId)) {
                    val firstName = user.userMetadata?.get("first_name")?.toString()?.trim('"') ?: "Nieznany"
                    result[userId] = firstName
                }
            }

            result
        } catch (e: Exception) {
            Log.e("F1Repository", "Error fetching authors info", e)
            emptyMap()
        }
    }

}




