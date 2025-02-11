package zeroone.developers.clinicapp

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface UserService {
    fun getAll(pageable: Pageable): Page<UserResponse>
    fun getAll(): List<UserResponse>
    fun getOne(id: Long): UserResponse
    fun create(request: UserCreateRequest): UserResponse
    fun update(id: Long, request: UserUpdateRequest): UserResponse
    fun delete(id: Long)
}

interface EmployeeService {
    fun getAll(pageable: Pageable): Page<EmployeeResponse>
    fun getAll(): List<EmployeeResponse>
    fun getOne(id: Long): EmployeeResponse
    fun create(request: EmployeeCreateRequest): EmployeeResponse
    fun update(id: Long, request: EmployeeUpdateRequest): EmployeeResponse
    fun delete(id: Long)
}

interface PatientService {
    fun getAll(pageable: Pageable): Page<PatientResponse>
    fun getAll(): List<PatientResponse>
    fun getOne(id: Long): PatientResponse
    fun create(request: PatientCreateRequest): PatientResponse
    fun update(id: Long, request: PatientUpdateRequest): PatientResponse
    fun delete(id: Long)
}

interface ServiceService {
    fun getAll(pageable: Pageable): Page<ServiceResponse>
    fun getAll(): List<ServiceResponse>
    fun getOne(id: Long): ServiceResponse
    fun create(request: ServiceCreateRequest): ServiceResponse
    fun update(id: Long, request: ServiceUpdateRequest): ServiceResponse
    fun useService(request: ServiceUsageRequest): ServiceResult
    fun delete(id: Long)
}

interface TransactionService {
    fun getAll(pageable: Pageable): Page<TransactionResponse>
    fun getAll(): List<TransactionResponse>
    fun getOne(id: Long): TransactionResponse
    fun getPatientTransactions(patientId: Long): List<TransactionResponse>
    fun create(request: TransactionCreateRequest): TransactionResponse
    fun delete(id: Long)
}

interface AppointmentService {
    fun getAll(pageable: Pageable): Page<AppointmentResponse>
    fun getAll(): List<AppointmentResponse>
    fun getOne(id: Long): AppointmentResponse
    fun createAppointment(request: AppointmentCreateRequest): AppointmentResponse
    fun completeAppointment(appointmentId: Long): AppointmentResponse
    fun cancelMissedAppointments()
    fun delete(id: Long)
}

interface TestResultService {
    fun getAll(pageable: Pageable): Page<TestResultResponse>
    fun getAll(): List<TestResultResponse>
    fun getOne(id: Long): TestResultResponse
    fun create(request: TestResultCreateRequest): TestResultResponse
    fun update(id: Long, request: TestResultUpdateRequest): TestResultResponse
    fun delete(id: Long)
}

interface ScheduleService {
    fun getAll(pageable: Pageable): Page<ScheduleResponse>
    fun getAll(): List<ScheduleResponse>
    fun getOne(id: Long): ScheduleResponse
    fun getAllDoctorAvailableSlot(doctorId: Long, dayOfWeek: DayOfWeek): List<ScheduleResponse>
    fun create(request: ScheduleCreateRequest): List<ScheduleResponse>
    fun delete(id: Long)
}

interface DepartmentService {
    fun getAll(pageable: Pageable): Page<DepartmentResponse>
    fun getAll(): List<DepartmentResponse>
    fun getOne(id: Long): DepartmentResponse
    fun create(request: DepartmentCreateRequest): DepartmentResponse
    fun update(id: Long, request: DepartmentUpdateRequest): DepartmentResponse
    fun delete(id: Long)
}

interface ClinicService {
    fun getAll(pageable: Pageable): Page<ClinicResponse>
    fun getAll(): List<ClinicResponse>
    fun getOne(id: Long): ClinicResponse
    fun create(request: ClinicCreateRequest): ClinicResponse
    fun update(id: Long, request: ClinicUpdateRequest): ClinicResponse
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

    override fun create(request: UserCreateRequest): UserResponse {
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


@Service
class EmployeeServiceImpl(
    private val employeeMapper: EmployeeMapper,
    private val userRepository: UserRepository,
    private val employeeRepository: EmployeeRepository,
    private val serviceRepository: ServiceRepository,
    private val clinicRepository: ClinicRepository,
    private val entityManager: EntityManager,
    private val userMapper: UserMapper
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


@Service
class ScheduleServiceImpl(
    private val entityManager: EntityManager,
    private val scheduleMapper: ScheduleMapper,
    private val scheduleRepository: ScheduleRepository,
    private val employeeRepository: EmployeeRepository
) : ScheduleService {

    override fun getAll(pageable: Pageable): Page<ScheduleResponse> {
        val schedulesPage = scheduleRepository.findAllNotDeletedForPageable(pageable)
        return schedulesPage.map { scheduleMapper.toDto(it) }
    }

    override fun getAll(): List<ScheduleResponse> {
        return scheduleRepository.findAllNotDeleted().map {
            scheduleMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): ScheduleResponse {
        scheduleRepository.findByIdAndDeletedFalse(id)?.let {
            return scheduleMapper.toDto(it)
        } ?: throw ScheduleNotFoundException()
    }

    override fun getAllDoctorAvailableSlot(doctorId: Long, dayOfWeek: DayOfWeek): List<ScheduleResponse> {
        val slots = scheduleRepository.findAvailableSlotsByDoctorAndDay(doctorId, dayOfWeek)
        return slots.map { ScheduleResponse(
            it.id,
            it.doctor.user.username,
            it.dayOfWeek,
            it.startTime,
            it.endTime,
            it.date) }
    }

    // Process 1.
    override fun create(request: ScheduleCreateRequest): List<ScheduleResponse> {
        val doctor = employeeRepository.findDoctorById(request.doctorId)
            .orElseThrow { EmployeeNotFoundException() }
        val schedules = mutableListOf<Schedule>()

        val workPeriods = listOf(
            request.startTime to LocalTime.of(12, 0),
            LocalTime.of(13, 0) to request.endTime
        )
        for ((start, end) in workPeriods) {
            if (start.isBefore(end)) {
                var timeSlot = start
                while (timeSlot.plusMinutes(15).isBefore(end) ||
                    timeSlot.plusMinutes(15) == end
                ) {
                    val schedule = scheduleMapper.toEntity(request, doctor)
                        .apply {
                            this.startTime = timeSlot
                            this.endTime = timeSlot.plusMinutes(15)
                        }
                    schedules.add(schedule)
                    timeSlot = timeSlot.plusMinutes(15)
                }
            }
        }
        val savedSchedules = scheduleRepository.saveAll(schedules)
        return savedSchedules.map { scheduleMapper.toDto(it) }
    }

    @Transactional
    override fun delete(id: Long) {
        scheduleRepository.trash(id) ?: throw ScheduleNotFoundException()
    }
}


@Service
class AppointmentServiceImpl(
    private val appointmentMapper: AppointmentMapper,
    private val entityManager: EntityManager,
    private val appointmentRepository: AppointmentRepository,
    private val scheduleRepository: ScheduleRepository,
    private val patientRepository: PatientRepository,
    private val employeeRepository: EmployeeRepository
) : AppointmentService {

    override fun getAll(pageable: Pageable): Page<AppointmentResponse> {
        val appointmentsPage = appointmentRepository.findAllNotDeletedForPageable(pageable)
        return appointmentsPage.map { appointmentMapper.toDto(it) }
    }

    override fun getAll(): List<AppointmentResponse> {
        return appointmentRepository.findAllNotDeleted().map {
            appointmentMapper.toDto(it)
        }
    }

    override fun getOne(id: Long): AppointmentResponse {
        appointmentRepository.findByIdAndDeletedFalse(id)?.let {
            return appointmentMapper.toDto(it)
        } ?: throw AppointmentNotFoundException()
    }

    //Process 2.
    @Transactional
    override fun createAppointment(request: AppointmentCreateRequest): AppointmentResponse {
        val patient = patientRepository.findById(request.patientId).orElseThrow { PatientNotFoundException() }
        val doctor = employeeRepository.findById(request.doctorId).orElseThrow { EmployeeNotFoundException() }

        val customDayOfWeek = javaToCustomDayOfWeek(request.orderedDate.dayOfWeek)

        val workPeriods = listOf(
            request.orderedTime to LocalTime.of(12, 0),
            LocalTime.of(13, 0) to request.orderedTime
        )
        val schedulesToUpdate = mutableListOf<Schedule>()
        for ((start, end) in workPeriods) {
            val schedule = scheduleRepository.findByDoctorAndTime(
                request.doctorId,
                customDayOfWeek,
                start)
                ?: throw SlotNotAvailableException()

            if (schedule.scheduleStatus != ScheduleStatus.AVAILABLE) {
                throw SlotAlreadyBookedException()
            }

            if (request.orderedDate != schedule.date) {
                throw DateMismatchException()
            }

            schedule.scheduleStatus = ScheduleStatus.BOOKED
            schedulesToUpdate.add(schedule)
        }
        scheduleRepository.saveAll(schedulesToUpdate)
        val appointment = Appointment(
            patient = patient,
            doctor = doctor,
            orderedDate = request.orderedDate,
            orderedTime = request.orderedTime,
            orderStatus = OrderStatus.PENDING
        )
        val savedAppointment = appointmentRepository.save(appointment)
        return appointmentMapper.toDto(savedAppointment)
    }

    fun javaToCustomDayOfWeek(javaDay: java.time.DayOfWeek): DayOfWeek {
        return DayOfWeek.valueOf(javaDay.name) // MONDAY -> MONDAY, TUESDAY -> TUESDAY
    }


    @Transactional
    override fun completeAppointment(appointmentId: Long): AppointmentResponse {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { AppointmentNotFoundException() }

        appointment.orderStatus = OrderStatus.COMPLETED
        val savedAppointment = appointmentRepository.save(appointment)
        return appointmentMapper.toDto(savedAppointment)
    }


    @Transactional
    override fun cancelMissedAppointments() {
        val now = LocalDateTime.now()
        val missedAppointments = appointmentRepository.findAllMissedAppointments(now.toLocalDate(), now.toLocalTime())
        missedAppointments.forEach {
            it.orderStatus = OrderStatus.CANCELLED
            appointmentRepository.save(it)
        }
    }

    @Transactional
    override fun delete(id: Long) {
        appointmentRepository.trash(id) ?: throw AppointmentNotFoundException()
    }
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




