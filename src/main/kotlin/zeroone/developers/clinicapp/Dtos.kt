package zeroone.developers.clinicapp

import jakarta.persistence.Column
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class BaseMessage(val code: Int, val message: String?)

data class UserCreateRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
    val address: String,
    val role: Role,
    val gender: Gender
)

data class UserResponse(
    val id: Long?,
    val username: String?,
    val password: String?,
    val fullName: String?,
    val phoneNumber: String?,
    val address: String?,
    val role: Role?,
    val gender: Gender?
)

data class UserUpdateRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
    val address: String,
    val role: Role,
    val gender: Gender
)
//
data class EmployeeCreateRequest(
    val userId: Long,
    val experience: Long,
    val degree: String,
    //val serviceId: Long?,
    val clinicId: Long
)

data class EmployeeResponse(
    val id: Long?,
    val userId: Long?,
    val userFullName: String?,
    val role: Role?,
    val experience: Long?,
    val degree: String?,
//    val serviceId: Long?,
//    val serviceName: String?,
    val clinicId: Long?,
    val clinicName: String?
)

data class EmployeeUpdateRequest(
    val experience: Long,
    val degree: String
)

data class PatientCreateRequest(
    val userId: Long,
    val birthDate: LocalDate,
    val address: String
)

data class PatientResponse(
    val id: Long?,
    val userId: Long?,
    val role: Role?,
    val birthDate: LocalDate?,
    val address: String?,
    val userFullName: String?
)

data class PatientUpdateRequest(
    val birthDate: LocalDate,
    val address: String
)

data class ServiceCreateRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val departmentId: Long
)

data class ServiceUsageRequest(
    val patientId: Long,
    val serviceId: Long,
    val doctorId: Long?,
    val paymentMethod: PaymentMethod
)

data class ServiceResponse(
    val id: Long?,
    val name: String?,
    val description: String?,
    val price: BigDecimal?,
    val departmentName: String?
)

data class ServiceUpdateRequest(
    val name: String,
    val description: String,
    val price: BigDecimal
)

data class ServiceResultResponse(
    val serviceId: Long?,
    val patientId: Long?,
    val doctorId: Long?,
    val result: String,
    val date: LocalDateTime = LocalDateTime.now(),
)

data class DepartmentCreateRequest(
    val name: String,
)

data class DepartmentResponse(
    val id: Long?,
    val name: String?,
)

data class DepartmentUpdateRequest(
    val name: String,
)

data class ClinicCreateRequest(
    val name: String,
    val address: String
)

data class ClinicResponse(
    val id: Long?,
    val name: String?,
    val address: String?
)

data class ClinicUpdateRequest(
    val name: String,
    val address: String
)

data class ScheduleCreateRequest(
    val doctorId: Long,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)

data class ScheduleResponse(
    val id: Long?,
    val doctorName: String?,
    val dayOfWeek: DayOfWeek?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val date: LocalDate?
)

data class ScheduleUpdateRequest(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)

data class TransactionCreateRequest(
    val patientId: Long,
    val serviceId: Long,
    val amount: BigDecimal,
    val paymentMethod: PaymentMethod,
    val doctorId: Long
)

data class TransactionResponse(
    val id: Long?,
    val patientName: String?,
    val serviceName: String?,
    val amount: BigDecimal?,
    val paymentMethod: PaymentMethod?,
    val doctorName: String?
)

data class TransactionUpdateRequest(
    val amount: BigDecimal,
    val paymentMethod: PaymentMethod
)

data class AppointmentCreateRequest(
    val patientId: Long,
    val doctorId: Long,
    val orderedDate: LocalDate,
    val orderedTime: LocalTime
)

data class AppointmentResponse(
    val id: Long?,
    val patientName: String?,
    val doctorName: String?,
    val orderedDate: LocalDate?,
    val orderedTime: LocalTime?,
    val orderStatus: OrderStatus?
)

data class AppointmentUpdateRequest(
    val orderedDate: LocalDate,
    val orderedTime: LocalTime,
    val orderStatus: OrderStatus
)

data class TestResultCreateRequest(
    val patientId: Long,
    val serviceId: Long,
    val result: String,
    val doctorId: Long
)

data class TestResultResponse(
    val id: Long?,
    val patientName: String?,
    val serviceName: String?,
    val result: String?,
    val doctorName: String?
)

data class TestResultUpdateRequest(
    val result: String
)
