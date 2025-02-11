package zeroone.developers.clinicapp

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): List<T>
    fun findAllNotDeletedForPageable(pageable: Pageable): Page<T>
    fun saveAndRefresh(t: T): T
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    private val entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }
    val sortedDescending = Specification<T> { root, query,cb->query!!.orderBy(cb.desc(root.get<Any>("id")))
        cb.conjunction()
    }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeleted(pageable: Pageable): List<T> = findAll(isNotDeletedSpecification, pageable).content
    override fun findAllNotDeletedForPageable(pageable: Pageable): Page<T> =
        findAll(isNotDeletedSpecification.and(sortedDescending), pageable)

    @Transactional
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }


    @Transactional
    override fun saveAndRefresh(t: T): T {
        return save(t).apply { entityManager.refresh(this) }
    }
}

@Repository
interface UserRepository : BaseRepository<User> {
    fun findByUsernameAndDeletedFalse(username: String): User?

    fun findByPhoneNumberAndDeletedFalse(phoneNumber: String): User?

    @Query("""
        select u from users u
        where u.id != :id
        and u.username = :username
        and u.deleted = false 
    """)
    fun findByUsername(@Param("id") id: Long, @Param("username") username: String): User?

    @Query("""
        select u from users u
        where u.id != :id
        and u.phoneNumber = :phoneNumber
        and u.deleted = false 
    """)
    fun findByPhoneNumber(@Param("id") id: Long, @Param("phoneNumber") phoneNumber: String): User?
}

@Repository
interface EmployeeRepository : BaseRepository<Employee> {

    @Query(
        value = """
        select e.* from employees e 
        join users u on e.user_id = u.id 
        where e.id = :employeeId 
        and u.role = 'DOCTOR'
    """,
        nativeQuery = true
    )
    fun findDoctorById(@Param("employeeId") employeeId: Long): Optional<Employee>


    @Query(value = "select count(*) > 0 from employees e where e.id = :id", nativeQuery = true)
    fun existsByDoctorId(@Param("id") id: Long?): Boolean
}

@Repository
interface PatientRepository : BaseRepository<Patient> {

    @Query(value = "select count(*) > 0 from patients p where p.id = :id", nativeQuery = true)
    fun existsByPatientId(@Param("id") id: Long?): Boolean

}

@Repository
interface ServiceRepository : BaseRepository<Services> {

    @Query(value = "select count(*) > 0 from services s where s.id = :id", nativeQuery = true)
    fun existsByServiceId(@Param("id") id: Long?): Boolean

    fun findByNameAndDeletedFalse(name: String): Services?

    @Query("""
        select s from services s
        where s.id != :id
        and s.name = :name
        and s.deleted = false 
    """)
    fun findByName(@Param("id") id: Long, @Param("name") name: String): Services?
}

@Repository
interface ServiceResultRepository : BaseRepository<ServiceResult> {


}

@Repository
interface TransactionRepository : BaseRepository<Transaction> {
    @Query(value = "select * from transactions where patient_id = :patientId", nativeQuery = true)
    fun findByPatientId(@Param("patientId") patientId: Long?): List<Transaction?>?

}

@Repository
interface AppointmentRepository : BaseRepository<Appointment> {
    @Query(value = """
        select * from appointment 
        where date = :currentDate 
        and start_time <= :currentTime 
        and order_status = 'ACTIVE'
        """,
        nativeQuery = true
    )
    fun findAllMissedAppointments(
        @Param("currentDate") currentDate: LocalDate,
        @Param("currentTime") currentTime: LocalTime
    ): List<Appointment>

    @Query(
        value = "select * from appointment where patient_id = :patientId" +
                " and doctor_id = :doctorId and (order_status = 'PENDING'" +
                " or order_status = 'CONFIRMED') limit 1",
        nativeQuery = true
    )
    fun findByPatientAndDoctor(
        @Param("patientId") patientId: Long,
        @Param("doctorId") doctorId: Long): Appointment?

}

@Repository
interface TestResultRepository : BaseRepository<TestResult> {

}

@Repository
interface ScheduleRepository : BaseRepository<Schedule> {

    @Query("select s from schedules s where s.doctor.id = :doctorId and s.dayOfWeek = :dayOfWeek and s.scheduleStatus = 'AVAILABLE'")
    fun findAvailableSlotsByDoctorAndDay(doctorId: Long, dayOfWeek: DayOfWeek): List<Schedule>

    @Query("""
        select s from schedules s 
        where s.doctor.id = :doctorId 
        and s.dayOfWeek = :dayOfWeek
        and :startTime between s.startTime and s.endTime
        """)
    fun findByDoctorAndTime(doctorId: Long, dayOfWeek: DayOfWeek, startTime: LocalTime): Schedule?

}

@Repository
interface DepartmentRepository : BaseRepository<Department> {
    fun findByNameAndDeletedFalse(name: String): Department?

    @Query("""
        select d from departments d
        where d.id != :id
        and d.name = :name
        and d.deleted = false 
    """)
    fun findByName(@Param("id") id: Long, @Param("name") name: String): Department?

    @Query(value = "select count(*) > 0 from departments d where d.id = :id", nativeQuery = true)
    fun existsByDepartmentId(@Param("id") id: Long?): Boolean
}

@Repository
interface ClinicRepository : BaseRepository<Clinic> {
    fun findByNameAndDeletedFalse(name: String): Clinic?

    @Query("""
        select c from clinics c
        where c.id != :id
        and c.name = :name
        and c.deleted = false 
    """)
    fun findByName(@Param("id") id: Long, @Param("name") name: String): Clinic?
}
