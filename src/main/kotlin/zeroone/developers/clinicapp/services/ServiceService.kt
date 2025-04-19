package zeroone.developers.clinicapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*


interface ServiceService {
    fun getAll(pageable: Pageable): Page<ServiceResponse>
    fun getAll(): List<ServiceResponse>
    fun getOne(id: Long): ServiceResponse
    fun create(request: ServiceCreateRequest): ServiceResponse
    fun update(id: Long, request: ServiceUpdateRequest): ServiceResponse
    fun useService(request: ServiceUsageRequest): ServiceResult
    fun delete(id: Long)
}


@Service
class ServiceServiceImpl(
    private val serviceMapper: ServiceMapper,
    private val entityManager: EntityManager,
    private val serviceRepository: ServiceRepository,
    private val serviceResultRepository: ServiceResultRepository,
    private val departmentRepository: DepartmentRepository,
    private val transactionService: TransactionService,
    private val patientRepository: PatientRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionMapper: TransactionMapper,
    private val employeeRepository: EmployeeRepository
) : ServiceService {

    override fun getAll(pageable: Pageable): Page<ServiceResponse> {
        val servicesPage = serviceRepository.findAllNotDeletedForPageable(pageable)
        return servicesPage.map { serviceMapper.toDto(it) }
    }

    override fun getAll(): List<ServiceResponse> {
        return serviceRepository.findAllNotDeleted().map {
            serviceMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): ServiceResponse {
        serviceRepository.findByIdAndDeletedFalse(id)?.let {
            return serviceMapper.toDto(it)
        } ?: throw ServiceNotFoundException()
    }

    override fun create(request: ServiceCreateRequest): ServiceResponse {
        val existingService = serviceRepository.findByNameAndDeletedFalse(request.name)
        if (existingService != null) throw ServiceAlreadyExistsException()
        val existsDepartment = departmentRepository.existsByDepartmentId(request.departmentId)
        if (!existsDepartment) throw DepartmentNotFoundException()
        val department = entityManager.getReference(
            Department::class.java, request.departmentId
        )
        val toEntity = serviceMapper.toEntity(request, department)
        val savedService = serviceRepository.save(toEntity)
        return serviceMapper.toDto(savedService)
    }

    override fun update(id: Long, request: ServiceUpdateRequest): ServiceResponse {
        val service = serviceRepository.findByIdAndDeletedFalse(id) ?: throw ServiceNotFoundException()
        serviceRepository.findByName(id, request.name)?.let { throw ServiceAlreadyExistsException() }

        val updateService = serviceMapper.updateEntity(service, request)
        val savedService = serviceRepository.save(updateService)
        return serviceMapper.toDto(savedService)
    }

    override fun useService(request: ServiceUsageRequest): ServiceResult {
        val patient = patientRepository.findById(request.patientId)
            .orElseThrow { throw PatientNotFoundException() }

        val service = serviceRepository.findById(request.serviceId)
            .orElseThrow { throw ServiceNotFoundException() }

        val doctor = request.doctorId?.let {
            employeeRepository.findById(it).orElseThrow { throw EmployeeNotFoundException() }
        }
        val transaction = Transaction(
            patient = patient,
            service = service,
            amount = service.price,
            paymentMethod = request.paymentMethod,
            doctor = doctor!!
        )
        transactionRepository.save(transaction)
        // 🟢 4. Xizmat natijasini yaratish
        val resultText = generateResult(service)
        val serviceResult = ServiceResult(
            service = service,
            patient = patient,
            doctor = doctor,
            result = resultText
        )
        return serviceResultRepository.save(serviceResult)
    }

    private fun generateResult(service: Services): String {
        return """
        **${service.name} natijalari:**
        - Xizmat tavsifi: ${service.description}
        - Narxi: ${service.price} so‘m
        - Holat: Xizmat bajarildi, natijalar shifokorga taqdim etildi.
        **Tavsiya:** Agar savollaringiz bo‘lsa, vrach bilan maslahat qiling.
    """.trimIndent()
    }


    @Transactional
    override fun delete(id: Long) {
        serviceRepository.trash(id) ?: throw ServiceNotFoundException()
    }
}