package zeroone.developers.clinicapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*


interface TestResultService {
    fun getAll(pageable: Pageable): Page<TestResultResponse>
    fun getAll(): List<TestResultResponse>
    fun getOne(id: Long): TestResultResponse
    fun create(request: TestResultCreateRequest): TestResultResponse
    fun update(id: Long, request: TestResultUpdateRequest): TestResultResponse
    fun delete(id: Long)
}


@Service
class TestResultServiceImpl(
    private val entityManager: EntityManager,
    private val testResultMapper: TestResultMapper,
    private val patientRepository: PatientRepository,
    private val serviceRepository: ServiceRepository,
    private val employeeRepository: EmployeeRepository,
    private val testResultRepository: TestResultRepository
) : TestResultService {

    override fun getAll(pageable: Pageable): Page<TestResultResponse> {
        val testResultsPage = testResultRepository.findAllNotDeletedForPageable(pageable)
        return testResultsPage.map { testResultMapper.toDto(it) }
    }

    override fun getAll(): List<TestResultResponse> {
        return testResultRepository.findAllNotDeleted().map {
            testResultMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): TestResultResponse {
        testResultRepository.findByIdAndDeletedFalse(id)?.let {
            return testResultMapper.toDto(it)
        } ?: throw TestResultNotFoundException()
    }

    override fun create(request: TestResultCreateRequest): TestResultResponse {
        val patient = patientRepository.findById(request.patientId)
            .orElseThrow { PatientNotFoundException() }

        val service = serviceRepository.findById(request.serviceId)
            .orElseThrow { ServiceNotFoundException() }

        val doctor = employeeRepository.findById(request.doctorId)
            .orElseThrow { EmployeeNotFoundException() }

        val toEntity = testResultMapper.toEntity(request, patient, service, doctor)
        val savedTestResult = testResultRepository.save(toEntity)
        return testResultMapper.toDto(savedTestResult)
    }

    override fun update(id: Long, request: TestResultUpdateRequest): TestResultResponse {
        val testResult = testResultRepository.findByIdAndDeletedFalse(id) ?: throw TestResultNotFoundException()

        val updateResult = testResultMapper.updateEntity(testResult, request)
        val savedResult = testResultRepository.save(updateResult)
        return testResultMapper.toDto(savedResult)
    }

    @Transactional
    override fun delete(id: Long) {
        testResultRepository.trash(id) ?: throw TestResultNotFoundException()
    }
}