package zeroone.developers.clinicapp

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler(private val errorMessageSource: ResourceBundleMessageSource) {

    @ExceptionHandler(ClinicException::class)
    fun handleAccountException(exception: ClinicException): ResponseEntity<BaseMessage> {
        return ResponseEntity.badRequest().body(exception.getErrorMessage(errorMessageSource))
    }
}

sealed class ClinicException() : RuntimeException() {

    abstract fun errorCode(): ErrorCode
    open fun getArguments(): Array<Any?>? = null

    fun getErrorMessage(resourceBundleMessageSource: ResourceBundleMessageSource): BaseMessage {
        val message = try {
            resourceBundleMessageSource.getMessage(
                errorCode().name, getArguments(), LocaleContextHolder.getLocale()
            )
        } catch (e: Exception) {
            e.message ?: "error"
        }
        return BaseMessage(errorCode().code, message)
    }
}


class TokenNotFoundException(val id: Long) : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.TOKEN_NOT_FOUND
}

class UserNotAuthenticatedException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.USER_NOT_AUTHENTICATED
}

class UserNotFoundException(private val id: Long) : ClinicException() {
    override fun getArguments(): Array<Any?> = arrayOf(id)
    override fun errorCode(): ErrorCode = ErrorCode.USER_NOT_FOUND
}

class UserAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.USER_ALREADY_EXISTS
}

class UsernameInvalidException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.USERNAME_INVALID
}

class EmployeeNotFoundException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.EMPLOYEE_NOT_FOUND
}

class EmployeeAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.EMPLOYEE_ALREADY_EXISTS
}

class PatientNotFoundException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.PATIENT_NOT_FOUND
}

class PatientAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.PATIENT_ALREADY_EXISTS
}

class DepartmentNotFoundException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.DEPARTMENT_NOT_FOUND
}

class DepartmentAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.DEPARTMENT_ALREADY_EXISTS
}

class ScheduleNotFoundException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.SCHEDULE_NOT_FOUND
}

class ScheduleAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.SCHEDULE_ALREADY_EXISTS
}

class SlotNotAvailableException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.SLOT_IS_NOT_AVAILABLE
}

class SlotAlreadyBookedException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.SLOT_ALREADY_BOOKED
}

class ClinicNotFoundException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.CLINIC_NOT_FOUND
}

class ClinicAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.CLINIC_ALREADY_EXISTS
}

class TestResultNotFoundException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.TEST_RESULTS_NOT_FOUND
}

class TestResultAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.TEST_RESULTS_ALREADY_EXISTS
}

class TransactionNotFoundException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.TRANSACTION_NOT_FOUND
}

class TransactionAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.TRANSACTION_ALREADY_EXISTS
}

class ServiceNotFoundException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.SERVICE_NOT_FOUND
}

class ServiceAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.SERVICE_ALREADY_EXISTS
}

class AppointmentNotFoundException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.APPOINTMENT_NOT_FOUND
}

class AppointmentAlreadyExistsException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.APPOINTMENT_ALREADY_EXISTS
}

class DateMismatchException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.DATE_MISS_MATCH
}

class BadRequestException() : ClinicException() {
    override fun errorCode(): ErrorCode = ErrorCode.BAD_REQUEST
}


