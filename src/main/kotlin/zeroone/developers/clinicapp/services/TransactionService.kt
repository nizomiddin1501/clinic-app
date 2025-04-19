package zeroone.developers.clinicapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*


interface TransactionService {
    fun getAll(pageable: Pageable): Page<TransactionResponse>
    fun getAll(): List<TransactionResponse>
    fun getOne(id: Long): TransactionResponse
    fun getPatientTransactions(patientId: Long): List<TransactionResponse>
    fun create(request: TransactionCreateRequest): TransactionResponse
    fun delete(id: Long)
}


@Service
class TransactionServiceImpl(
    private val entityManager: EntityManager,
    private val patientRepository: PatientRepository,
    private val serviceRepository: ServiceRepository,
    private val transactionMapper: TransactionMapper,
    private val employeeRepository: EmployeeRepository,
    private val appointmentRepository: AppointmentRepository,
    private val transactionRepository: TransactionRepository,
) : TransactionService {

    override fun getAll(pageable: Pageable): Page<TransactionResponse> {
        val transactionsPage = transactionRepository.findAllNotDeletedForPageable(pageable)
        return transactionsPage.map { transactionMapper.toDto(it) }
    }

    override fun getAll(): List<TransactionResponse> {
        return transactionRepository.findAllNotDeleted().map {
            transactionMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): TransactionResponse {
        transactionRepository.findByIdAndDeletedFalse(id)?.let {
            return transactionMapper.toDto(it)
        } ?: throw TransactionNotFoundException()
    }

    override fun getPatientTransactions(patientId: Long): List<TransactionResponse> {
        return transactionRepository.findByPatientId(patientId)?.map { transaction ->
            TransactionResponse(
                id = transaction!!.id,
                serviceName = transaction.service.name,
                amount = transaction.amount,
                patientName = transaction.patient.user.fullName,
                paymentMethod = transaction.paymentMethod,
                doctorName = transaction.doctor.user.fullName
            )
        } ?: throw PatientNotFoundException()
    }

    //Process 3.
    @Transactional
    override fun create(request: TransactionCreateRequest): TransactionResponse {
        val patient = patientRepository.findById(request.patientId).orElseThrow { PatientNotFoundException() }
        val service = serviceRepository.findById(request.serviceId).orElseThrow { ServiceNotFoundException() }
        val doctor = employeeRepository.findById(request.doctorId).orElseThrow { EmployeeNotFoundException() }
        val appointment = appointmentRepository.findByPatientAndDoctor(patient.id!!, doctor.id!!)
            ?: throw AppointmentNotFoundException()

        val transaction = Transaction(
            patient = patient,
            service = service,
            amount = request.amount,
            paymentMethod = request.paymentMethod,
            doctor = doctor)

        appointment.orderStatus = OrderStatus.CONFIRMED
        appointmentRepository.save(appointment)
        val savedTransaction = transactionRepository.save(transaction)
        return transactionMapper.toDto(savedTransaction)
    }

    @Transactional
    override fun delete(id: Long) {
        transactionRepository.trash(id) ?: throw TransactionNotFoundException()
    }
}