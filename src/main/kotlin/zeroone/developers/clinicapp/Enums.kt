package zeroone.developers.clinicapp

enum class Role {
    PATIENT, CASHIER, DOCTOR, DIRECTOR, LAB_TECHNICIAN
}

enum class OrderStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED
}

enum class Gender {
    MALE, FEMALE
}

enum class PaymentMethod {
    CASH, CARD
}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

enum class ScheduleStatus {
    AVAILABLE, BOOKED
}

enum class ErrorCode(val code: Int) {
    TOKEN_NOT_FOUND(0),
    USER_ALREADY_EXISTS(100),
    USER_NOT_FOUND(101),
    USERNAME_INVALID(102),
    USER_NOT_AUTHENTICATED(103),
    EMPLOYEE_NOT_FOUND(200),
    EMPLOYEE_ALREADY_EXISTS(201),
    PATIENT_NOT_FOUND(300),
    PATIENT_ALREADY_EXISTS(301),
    DEPARTMENT_NOT_FOUND(400),
    DEPARTMENT_ALREADY_EXISTS(401),
    SCHEDULE_NOT_FOUND(500),
    SCHEDULE_ALREADY_EXISTS(501),
    SLOT_IS_NOT_AVAILABLE(77),
    SLOT_ALREADY_BOOKED(78),
    APPOINTMENT_NOT_FOUND(502),
    APPOINTMENT_ALREADY_EXISTS(503),
    DATE_MISS_MATCH(504),
    CLINIC_NOT_FOUND(600),
    CLINIC_ALREADY_EXISTS(601),
    TEST_RESULTS_NOT_FOUND(700),
    TEST_RESULTS_ALREADY_EXISTS(701),
    TRANSACTION_NOT_FOUND(800),
    TRANSACTION_ALREADY_EXISTS(801),
    SERVICE_NOT_FOUND(900),
    SERVICE_ALREADY_EXISTS(901),
    BAD_REQUEST(777)
}
