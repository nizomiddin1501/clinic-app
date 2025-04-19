package zeroone.developers.clinicapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*
import java.time.LocalDateTime
import java.time.LocalTime


interface AppointmentService {
    fun getAll(pageable: Pageable): Page<AppointmentResponse>
    fun getAll(): List<AppointmentResponse>
    fun getOne(id: Long): AppointmentResponse
    fun createAppointment(request: AppointmentCreateRequest): AppointmentResponse
    fun completeAppointment(appointmentId: Long): AppointmentResponse
    fun cancelMissedAppointments()
    fun delete(id: Long)
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