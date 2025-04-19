package zeroone.developers.clinicapp.services

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*


interface EmployeeService {
    fun getAll(pageable: Pageable): Page<EmployeeResponse>
    fun getAll(): List<EmployeeResponse>
    fun getOne(id: Long): EmployeeResponse
    fun create(request: EmployeeCreateRequest): EmployeeResponse
    fun update(id: Long, request: EmployeeUpdateRequest): EmployeeResponse
    fun delete(id: Long)
}


@Service
class EmployeeServiceImpl(
    private val employeeMapper: EmployeeMapper,
    private val userRepository: UserRepository,
    private val employeeRepository: EmployeeRepository,
    private val clinicRepository: ClinicRepository,
) : EmployeeService {

    override fun getAll(pageable: Pageable): Page<EmployeeResponse> {
        val employeesPage = employeeRepository.findAllNotDeletedForPageable(pageable)
        return employeesPage.map { employeeMapper.toDto(it) }
    }

    override fun getAll(): List<EmployeeResponse> {
        return employeeRepository.findAllNotDeleted().map {
            employeeMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): EmployeeResponse {
        employeeRepository.findByIdAndDeletedFalse(id)?.let {
            return employeeMapper.toDto(it)
        } ?: throw EmployeeNotFoundException()
    }

    override fun create(request: EmployeeCreateRequest): EmployeeResponse {
        val user = userRepository.findByIdAndDeletedFalse(request.userId)
            ?: throw UserNotFoundException(request.userId)

        val clinic = clinicRepository.findByIdAndDeletedFalse(request.clinicId)
            ?: throw ClinicNotFoundException()

        val toEntity = employeeMapper.toEntity(request, user, clinic)
        val savedEntity = employeeRepository.save(toEntity)
        return employeeMapper.toDto(savedEntity)
    }

    override fun update(id: Long, request: EmployeeUpdateRequest): EmployeeResponse {
        val employee = employeeRepository.findByIdAndDeletedFalse(id) ?: throw EmployeeNotFoundException()
        val updateEmployee = employeeMapper.updateEntity(employee, request)
        val savedEmployee = employeeRepository.save(updateEmployee)
        return employeeMapper.toDto(savedEmployee)
    }

    @Transactional
    override fun delete(id: Long) {
        employeeRepository.trash(id) ?: throw EmployeeNotFoundException()
    }
}