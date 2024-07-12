package io.newsletter.api.domain.user.service


import io.newsletter.api.domain.user.data.dto.UserResponse
import io.happytalk.api.global.repository.cud.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {

        suspend fun selectUserByUsername(username: String): UserResponse {
            return userRepository.selectUserByUsername(username)
        }

}
