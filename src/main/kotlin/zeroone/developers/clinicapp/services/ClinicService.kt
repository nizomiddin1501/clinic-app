package zeroone.developers.clinicapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*


interface ClinicService {
    fun getAll(pageable: Pageable): Page<ClinicResponse>
    fun getAll(): List<ClinicResponse>
    fun getOne(id: Long): ClinicResponse
    fun create(request: ClinicCreateRequest): ClinicResponse
    fun update(id: Long, request: ClinicUpdateRequest): ClinicResponse
    fun delete(id: Long)
}


@Service
class ClinicServiceImpl(
    private val clinicMapper: ClinicMapper,
    private val entityManager: EntityManager,
    private val clinicRepository: ClinicRepository
) : ClinicService {

    override fun getAll(pageable: Pageable): Page<ClinicResponse> {
        val clinicsPage = clinicRepository.findAllNotDeletedForPageable(pageable)
        return clinicsPage.map { clinicMapper.toDto(it) }
    }

    override fun getAll(): List<ClinicResponse> {
        return clinicRepository.findAllNotDeleted().map {
            clinicMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): ClinicResponse {
        clinicRepository.findByIdAndDeletedFalse(id)?.let {
            return clinicMapper.toDto(it)
        } ?: throw ClinicNotFoundException()
    }

    override fun create(request: ClinicCreateRequest): ClinicResponse {
        if (clinicRepository.findByNameAndDeletedFalse(request.name) != null)
            throw ClinicAlreadyExistsException()
        val toEntity = clinicMapper.toEntity(request)
        val savedClinic = clinicRepository.save(toEntity)
        return clinicMapper.toDto(savedClinic)
    }

    override fun update(id: Long, request: ClinicUpdateRequest): ClinicResponse {
        val clinic = clinicRepository.findByIdAndDeletedFalse(id)
            ?: throw ClinicNotFoundException()
        clinicRepository.findByName(id, request.name)?.let { throw ClinicAlreadyExistsException() }

        val updateClinic = clinicMapper.updateEntity(clinic, request)
        val savedClinic = clinicRepository.save(updateClinic)
        return clinicMapper.toDto(savedClinic)
    }

    @Transactional
    override fun delete(id: Long) {
        clinicRepository.trash(id) ?: throw ClinicNotFoundException()
    }
}