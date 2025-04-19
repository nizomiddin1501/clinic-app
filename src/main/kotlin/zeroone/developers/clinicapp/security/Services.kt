package zeroone.developers.clinicapp.security

import io.jsonwebtoken.Jwts
import org.apache.catalina.core.Constants
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.BadCredentialsException
import zeroone.developers.clinicapp.UserRepository
import java.util.*


interface AuthService {
    fun authenticate(request: LoginRequest): TokenResponse
    fun refresh(request: RefreshRequest): TokenResponse
}


@Service
class AuthServiceImpl(
    private val userDetailsService: CustomUserDetailsService,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val jwtService: JwtService,
) : AuthService {
    override fun authenticate(request: LoginRequest): TokenResponse {
        val user = userDetailsService.loadUserByUsername(request.username)
        if (!passwordEncoder.matches(request.password, user.password)) throw BadCredentialsException()
        return jwtService.generateToken(user)
    }

    override fun refresh(request: RefreshRequest): TokenResponse {
        val claims = jwtService.getClaims(request.refreshToken)
            ?: throw InsufficientAuthenticationException("Not Valid Refresh token")

        val user = userDetailsService.loadUserByUsername(claims.username)
        return jwtService.generateToken(user)
    }
}

@Component
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): CustomUserDetails {
        val user = userRepository.findByUsernameAndDeletedFalse(username) ?: throw BadCredentialsException()
        if (user.status == Status.DEACTIVATED) throw DeactivatedUserException()
        return CustomUserDetails(user.password, user.username, user.role, user.id!!)
    }
}

data class CustomUserDetails(
    val myPassword: String,
    val myUserName: String,
    val role: Role,
    val id: Long
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(role.name)).toMutableList()
    }

    override fun getPassword(): String = myPassword

    override fun getUsername(): String = myUserName

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

}

@Component
class JwtService(
    @Value("\${application.security.jwt.secret-key}")
    private val secret: String,
    @Value("\${application.security.jwt.expiration}")
    private val expirationAccessToken: Long,
    @Value("\${application.security.jwt.refresh-token.expiration}")
    private val expirationRefreshToken: Long
) {
    private val issuer = "hrms-pro"
    fun generateToken(userDetails: CustomUserDetails): TokenResponse {
        val accessClaims: MutableMap<String, Any> = HashMap()
        val now = Date()
        val expiresIn = Date(now.time + expirationAccessToken)

        accessClaims[Constants.USERNAME] = userDetails.username
        accessClaims[Constants.USER_ID] = userDetails.id
        accessClaims[Constants.TOKEN_TYPE] = Constants.ACCESS_TOKEN

        val access = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .claims(accessClaims)
            .issuer(issuer)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expiresIn)
            .signWith(signKey()).compact()
        val refreshClaims: MutableMap<String, Any> = HashMap()

        refreshClaims[Constants.TOKEN_TYPE] = Constants.REFRESH_TOKEN
        refreshClaims[Constants.USERNAME] = userDetails.username
        refreshClaims[Constants.USER_ID] = userDetails.id

        val refreshToken = Jwts.builder()
            .issuer(issuer)
            .claims(refreshClaims)
            .id(UUID.randomUUID().toString())
            .expiration(Date(now.time + expirationRefreshToken))
            .signWith(signKey()).compact()
        return TokenResponse(access, refreshToken, expiresIn.time)
    }







