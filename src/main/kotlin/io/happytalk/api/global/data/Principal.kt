package io.happytalk.api.global.data

import io.happytalk.api.global.web.SecurityContextRepository
import io.jsonwebtoken.Claims
import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * [SecurityContextRepository.authenticate] 에서 principal 값을 단순히 wrapping 하기 위한 클래스
 *
 * - endpoint 컨트롤러에서 [AuthenticationPrincipal] 어노테이션 으로 넘긴 후 사용하기 위한 용도
 * - 꼭 Principal 일 필요는 없다. 변경이 필요하다 판단되면 고민하지 말고 바꾸자
 *
 * @author : LN
 * @since : 2023. 4. 17.
 */
data class Principal(

    val claims: Claims,
)
