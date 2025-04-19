package zeroone.developers.clinicapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*


interface DepartmentService {
    fun getAll(pageable: Pageable): Page<DepartmentResponse>
    fun getAll(): List<DepartmentResponse>
    fun getOne(id: Long): DepartmentResponse
    fun create(request: DepartmentCreateRequest): DepartmentResponse
    fun update(id: Long, request: DepartmentUpdateRequest): DepartmentResponse
    fun delete(id: Long)
}


@Service
class DepartmentServiceImpl(
    private val departmentMapper: DepartmentMapper,
    private val entityManager: EntityManager,
    private val departmentRepository: DepartmentRepository
) : DepartmentService {

    override fun getAll(pageable: Pageable): Page<DepartmentResponse> {
        val departmentsPage = departmentRepository.findAllNotDeletedForPageable(pageable)
        return departmentsPage.map { departmentMapper.toDto(it) }
    }

    override fun getAll(): List<DepartmentResponse> {
        return departmentRepository.findAllNotDeleted().map {
            departmentMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): DepartmentResponse {
        departmentRepository.findByIdAndDeletedFalse(id)?.let {
            return departmentMapper.toDto(it)
        } ?: throw DepartmentNotFoundException()
    }

    override fun create(request: DepartmentCreateRequest): DepartmentResponse {
        if (departmentRepository.findByNameAndDeletedFalse(request.name) != null)
            throw DepartmentAlreadyExistsException()
        val toEntity = departmentMapper.toEntity(request)
        val savedDepartment = departmentRepository.save(toEntity)
        return departmentMapper.toDto(savedDepartment)
    }

    override fun update(id: Long, request: DepartmentUpdateRequest): DepartmentResponse {
        val department = departmentRepository.findByIdAndDeletedFalse(id)
            ?: throw DepartmentNotFoundException()
        departmentRepository.findByName(id, request.name)?.let { throw DepartmentAlreadyExistsException() }

        val updateDepartment = departmentMapper.updateEntity(department, request)
        val savedDepartment = departmentRepository.save(updateDepartment)
        return departmentMapper.toDto(savedDepartment)
    }

    @Transactional
    override fun delete(id: Long) {
        departmentRepository.trash(id) ?: throw DepartmentNotFoundException()
    }
}