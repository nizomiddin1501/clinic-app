package zeroone.developers.clinicapp

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdAt: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var updatedAt: Date? = null,
    @CreatedBy var createdBy: Long? = null,
    @LastModifiedBy var updatedBy: Long? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false
)

@Entity(name = "users")
class User(
    @Column(nullable = false) var username: String,
    @Column(nullable = false) var password: String,
    @Column(nullable = false) var fullName: String,
    @Column(nullable = false) var phoneNumber: String,
    @Column(nullable = false) var address: String,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var gender: Gender,
) : BaseEntity()


@Entity(name = "patients")
class Patient(
    @OneToOne @JoinColumn(name = "user_id", nullable = false) val user: User,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var role: Role,
    @Column(nullable = false) var birthDate: LocalDate,
    @Column(nullable = false) var address: String,
) : BaseEntity()


@Entity(name = "employees")
class Employee(
    @Column(nullable = false) var experience: Long,
    @Column(nullable = false) var degree: String,
    @OneToOne @JoinColumn(name = "user_id", nullable = false) val user: User,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var role: Role,
    //@ManyToOne @JoinColumn(name = "service_id", nullable = true) val service: Services? = null,
    @ManyToOne @JoinColumn(name = "clinic_id", nullable = false) val clinic: Clinic,
    @ManyToMany  var patients:MutableList<Patient> = mutableListOf()
) : BaseEntity()


@Entity(name = "services")
class Services(
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var description: String,
    @Column(nullable = false) var price: BigDecimal,
    @ManyToOne val department: Department
) : BaseEntity()


@Entity(name = "service_results")
class ServiceResult(
    @ManyToOne val service: Services,
    @ManyToOne val patient: Patient,
    @ManyToOne val doctor: Employee?,
    @Column(nullable = false) val result: String,  // Tahlil natijasi (masalan, "Gemoglobin: 140g/L")
    @Column(nullable = false) val date: LocalDateTime = LocalDateTime.now()
) : BaseEntity()


@Entity(name = "departments")
class Department(
    @Column(nullable = false) var name: String,
) : BaseEntity()


@Entity(name = "clinics")
class Clinic(
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var address: String,
    @ManyToMany  var departments:MutableList<Department> = mutableListOf()
) : BaseEntity()


@Entity(name = "schedules")
class Schedule(
    @ManyToOne @JoinColumn(name = "doctor_id", nullable = false) var doctor: Employee,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var dayOfWeek: DayOfWeek,
    @Column(nullable = false) var startTime: LocalTime,
    @Column(nullable = false) var endTime: LocalTime,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var scheduleStatus: ScheduleStatus = ScheduleStatus.AVAILABLE,
    @Column(nullable = true) var date: LocalDate = LocalDate.now()
) : BaseEntity()


@Entity(name = "transaction")
class Transaction(
    @ManyToOne @JoinColumn(name = "patient_id", nullable = false) val patient: Patient,
    @ManyToOne @JoinColumn(name = "service_id", nullable = false) val service: Services,
    @Column(nullable = false) var amount: BigDecimal,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var paymentMethod: PaymentMethod,
    @ManyToOne @JoinColumn(name = "doctor_id", nullable = false) var doctor: Employee
) : BaseEntity()


@Entity(name = "appointment")
class Appointment(
    @ManyToOne @JoinColumn(name = "patient_id", nullable = false) val patient: Patient,
    @ManyToOne @JoinColumn(name = "doctor_id", nullable = false) val doctor: Employee,
    @Column(nullable = false) var orderedDate: LocalDate,
    @Column(nullable = false) var orderedTime: LocalTime,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var orderStatus: OrderStatus = OrderStatus.PENDING
) : BaseEntity()


@Entity(name = "test_results")
class TestResult(
    @ManyToOne @JoinColumn(name = "patient_id", nullable = false) val patient: Patient,
    @ManyToOne @JoinColumn(name = "service_id", nullable = false) val service: Services,
    @Column(nullable = false) var result: String,
    @ManyToOne var doctor: Employee,
) : BaseEntity()




