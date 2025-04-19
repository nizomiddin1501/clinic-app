package zeroone.developers.clinicapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*

interface UserService {
    fun getAll(pageable: Pageable): Page<UserResponse>
    fun getAll(): List<UserResponse>
    fun getOne(id: Long): UserResponse
    fun register(request: UserRegisterRequest): UserResponse
    fun update(id: Long, request: UserUpdateRequest): UserResponse
    fun delete(id: Long)
}


@Service
class UserServiceImpl(
    private val userMapper: UserMapper,
    private val entityManager: EntityManager,
    private val userRepository: UserRepository
) : UserService {

    override fun getAll(pageable: Pageable): Page<UserResponse> {
        val usersPage = userRepository.findAllNotDeletedForPageable(pageable)
        return usersPage.map { userMapper.toDto(it) }
    }

    override fun getAll(): List<UserResponse> {
        return userRepository.findAllNotDeleted().map {
            userMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): UserResponse {
        userRepository.findByIdAndDeletedFalse(id)?.let {
            return userMapper.toDto(it)
        } ?: throw UserNotFoundException(id)
    }

    override fun register(request: UserRegisterRequest): UserResponse {
        if (userRepository.findByUsernameAndDeletedFalse(request.username) != null)
            throw UserAlreadyExistsException()
        if (userRepository.findByPhoneNumberAndDeletedFalse(request.phoneNumber) != null)
            throw UserAlreadyExistsException()
        val toEntity = userMapper.toEntity(request)
        val savedUser = userRepository.save(toEntity)
        return userMapper.toDto(savedUser)
    }

    override fun update(id: Long, request: UserUpdateRequest): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(id)
            ?: throw UserNotFoundException(id)
        userRepository.findByUsername(id, request.username)
            ?.let { throw UserAlreadyExistsException() }
        userRepository.findByPhoneNumber(id, request.phoneNumber)
            ?.let { throw UserAlreadyExistsException() }
        val updateUser = userMapper.updateEntity(user, request)
        val savedUser = userRepository.save(updateUser)
        return userMapper.toDto(savedUser)
    }

    @Transactional
    override fun delete(id: Long) {
        userRepository.trash(id) ?: throw UserNotFoundException(id)
    }
}

