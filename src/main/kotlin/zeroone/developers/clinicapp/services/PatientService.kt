package zeroone.developers.clinicapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*

interface PatientService {
    fun getAll(pageable: Pageable): Page<PatientResponse>
    fun getAll(): List<PatientResponse>
    fun getOne(id: Long): PatientResponse
    fun create(request: PatientCreateRequest): PatientResponse
    fun update(id: Long, request: PatientUpdateRequest): PatientResponse
    fun delete(id: Long)
}


@Service
class PatientServiceImpl(
    private val patientMapper: PatientMapper,
    private val entityManager: EntityManager,
    private val userRepository: UserRepository,
    private val patientRepository: PatientRepository
) : PatientService {

    override fun getAll(pageable: Pageable): Page<PatientResponse> {
        val patientsPage = patientRepository.findAllNotDeletedForPageable(pageable)
        return patientsPage.map { patientMapper.toDto(it) }
    }

    override fun getAll(): List<PatientResponse> {
        return patientRepository.findAllNotDeleted().map {
            patientMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): PatientResponse {
        patientRepository.findByIdAndDeletedFalse(id)?.let {
            return patientMapper.toDto(it)
        } ?: throw PatientNotFoundException()
    }

    override fun create(request: PatientCreateRequest): PatientResponse {
        val user = userRepository.findByIdAndDeletedFalse(request.userId)
            ?: throw UserNotFoundException(request.userId)

        val toEntity = patientMapper.toEntity(request, user)
        val savedEntity = patientRepository.save(toEntity)
        return patientMapper.toDto(savedEntity)
    }

    override fun update(id: Long, request: PatientUpdateRequest): PatientResponse {
        val patient = patientRepository.findByIdAndDeletedFalse(id)
            ?: throw PatientNotFoundException()
        val updatePatient = patientMapper.updateEntity(patient, request)
        val savedPatient = patientRepository.save(updatePatient)
        return patientMapper.toDto(savedPatient)
    }

    @Transactional
    override fun delete(id: Long) {
        patientRepository.trash(id) ?: throw PatientNotFoundException()
    }
}