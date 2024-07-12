package io.happytalk.api.domain.user.service


import io.happytalk.api.domain.user.data.dto.UserResponse
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
