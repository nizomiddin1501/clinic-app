package zeroone.developers.clinicapp.services

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import zeroone.developers.clinicapp.*
import java.time.LocalTime


interface ScheduleService {
    fun getAll(pageable: Pageable): Page<ScheduleResponse>
    fun getAll(): List<ScheduleResponse>
    fun getOne(id: Long): ScheduleResponse
    fun getAllDoctorAvailableSlot(doctorId: Long, dayOfWeek: DayOfWeek): List<ScheduleResponse>
    fun create(request: ScheduleCreateRequest): List<ScheduleResponse>
    fun delete(id: Long)
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