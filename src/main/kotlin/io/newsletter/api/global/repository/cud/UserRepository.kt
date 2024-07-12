package io.newsletter.api.global.repository.cud

import io.newsletter.api.domain.user.data.dto.UserResponse
import io.happytalk.api.global.repository.BaseRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOne
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    @Qualifier("cudDatabaseClient")
    private val cudDatabaseClient: DatabaseClient,
) : BaseRepository() {

    /**
     *
     * @param email
     * @return
     */
    suspend fun selectUserByUsername(username: String): UserResponse {
        return cudDatabaseClient.sql("""
            select username, nickname
            from users 
            where username = :username
        """.trimIndent()
        ).bind("username", username)
            .mapTo(UserResponse::class.java)
            .awaitOne()
    }

}
