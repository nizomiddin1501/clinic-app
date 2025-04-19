package zeroone.developers.clinicapp

import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import zeroone.developers.clinicapp.services.*

const val BASE_API = "api/v1"

//@RestController
//@RequestMapping("$BASE_API/auth")
//class AuthController(
//    private val authService: AuthService
//) {
//
//    @PostMapping("login")
//    fun login(@RequestBody request: LoginRequest) =
//        authService.authenticate(request)
//
//
//    @PostMapping("refresh")
//    fun refresh(@RequestBody request: RefreshRequest) = authService.refresh(request)
//}


@RestController
@RequestMapping("$BASE_API/users")
class UserController(val service: UserService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @PostMapping
    fun create(@RequestBody @Valid request: UserRegisterRequest) = service.register(request)


    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: UserUpdateRequest) = service.update(id, request)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}


@RestController
@RequestMapping("$BASE_API/employees")
class EmployeeController(val service: EmployeeService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @PostMapping
    fun create(@RequestBody @Valid request: EmployeeCreateRequest) = service.create(request)


    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: EmployeeUpdateRequest) = service.update(id, request)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}


@RestController
@RequestMapping("$BASE_API/patients")
class PatientController(val service: PatientService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @PostMapping
    fun create(@RequestBody @Valid request: PatientCreateRequest) = service.create(request)


    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: PatientUpdateRequest) = service.update(id, request)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}


@RestController
@RequestMapping("$BASE_API/services")
class ServiceController(val service: ServiceService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @PostMapping
    fun create(@RequestBody @Valid request: ServiceCreateRequest) = service.create(request)


    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: ServiceUpdateRequest) = service.update(id, request)


    @PostMapping("/usage")
    fun useService(@RequestBody @Valid request: ServiceUsageRequest) = service.useService(request)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}


@RestController
@RequestMapping("$BASE_API/transactions")
class TransactionController(val service: TransactionService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @PostMapping
    fun create(@RequestBody @Valid request: TransactionCreateRequest) = service.create(request)


    @GetMapping("/patient/{patientId}")
    fun getPatientTransactions(@PathVariable patientId: Long) = service.getPatientTransactions(patientId)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}


@RestController
@RequestMapping("$BASE_API/appointments")
class AppointmentController(val service: AppointmentService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @PostMapping
    fun create(@RequestBody @Valid request: AppointmentCreateRequest) = service.createAppointment(request)

    @PostMapping("/complete/{appointmentId}")
    fun complete(@PathVariable appointmentId: Long): AppointmentResponse {
        return service.completeAppointment(appointmentId)
    }

    @PostMapping("/cancel-missed")
    fun cancelMissedAppointments() {
        service.cancelMissedAppointments()
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}


@RestController
@RequestMapping("$BASE_API/test-results")
class TestResultController(val service: TestResultService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @PostMapping
    fun create(@RequestBody @Valid request: TestResultCreateRequest) = service.create(request)


    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: TestResultUpdateRequest) = service.update(id, request)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}


@RestController
@RequestMapping("$BASE_API/schedules")
class ScheduleController(val service: ScheduleService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @GetMapping("/available-slots")
    fun getAllDoctorAvailableSlot(
        @RequestParam doctorId: Long,
        @RequestParam dayOfWeek: DayOfWeek): List<ScheduleResponse> {
        return service.getAllDoctorAvailableSlot(doctorId, dayOfWeek)
    }


    @PostMapping
    fun create(@RequestBody @Valid request: ScheduleCreateRequest) = service.create(request)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}


@RestController
@RequestMapping("$BASE_API/departments")
class DepartmentController(val service: DepartmentService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @PostMapping
    fun create(@RequestBody @Valid request: DepartmentCreateRequest) = service.create(request)


    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: DepartmentUpdateRequest) = service.update(id, request)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}


@RestController
@RequestMapping("$BASE_API/clinics")
class ClinicController(val service: ClinicService) {

    @GetMapping
    fun getAll() = service.getAll()


    @GetMapping("/page")
    fun getAll(pageable: Pageable) = service.getAll(pageable)


    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long) = service.getOne(id)


    @PostMapping
    fun create(@RequestBody @Valid request: ClinicCreateRequest) = service.create(request)


    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid request: ClinicUpdateRequest) = service.update(id, request)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}