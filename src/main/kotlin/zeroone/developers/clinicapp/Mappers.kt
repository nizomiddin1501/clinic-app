package zeroone.developers.clinicapp

import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toDto(user: User): UserResponse {
        return user.run {
            UserResponse(
                id = this.id,
                username = this.username,
                password = this.password,
                fullName = this.fullName,
                phoneNumber = this.phoneNumber,
                address = this.address,
                role = this.role,
                gender = this.gender
            )
        }
    }

    fun toEntity(createRequest: UserCreateRequest): User {
        return createRequest.run {
            User(
                username = this.username,
                password = this.password,
                fullName = this.fullName,
                phoneNumber = this.phoneNumber,
                address = this.address,
                role = this.role,
                gender = this.gender
            )
        }
    }

    fun updateEntity(user: User, updateRequest: UserUpdateRequest): User {
        return updateRequest.run {
            user.apply {
                updateRequest.username.let { this.username = it }
                updateRequest.password.let { this.password = it }
                updateRequest.fullName.let { this.fullName = it }
                updateRequest.phoneNumber.let { this.phoneNumber = it }
                updateRequest.address.let { this.address = it }
                updateRequest.role.let { this.role = it }
                updateRequest.gender.let { this.gender = it }
            }
        }
    }
}

@Component
class PatientMapper(
    private val userMapper: UserMapper) {

    fun toDto(patient: Patient): PatientResponse {
        return patient.run {
            PatientResponse(
                id = id,
                userId = user.id,
                role =  user.role,
                birthDate = birthDate,
                address = address,
                userFullName = user.fullName
            )
        }
    }

    fun toEntity(createRequest: PatientCreateRequest, user: User): Patient {
        return createRequest.run {
            Patient(
                user = user,
                birthDate = birthDate,
                address = address
            )
        }
    }

    fun updateEntity(patient: Patient, updateRequest: PatientUpdateRequest): Patient {
        return updateRequest.run {
            patient.apply {
                updateRequest.birthDate.let { this.birthDate = it }
                updateRequest.address.let { this.address = it }
            }
        }
    }
}


@Component
class EmployeeMapper(
    private val patientMapper: PatientMapper) {

    fun toDto(employee: Employee): EmployeeResponse {
        return employee.run {
            EmployeeResponse(
                id = id,
                userId = user.id,
                userFullName = user.fullName,
                role = user.role,
                experience = experience,
                degree = degree,
//                serviceId = service!!.id,
//                serviceName = service!!.name,
                clinicId = clinic.id,
                clinicName = clinic.name
            )
        }
    }

    fun toEntity(createRequest: EmployeeCreateRequest, user: User, clinic: Clinic): Employee {
        return createRequest.run {
            Employee(
                user = user,
                experience = experience,
                degree = degree,
                //service = service,
                clinic = clinic
            )
        }
    }

    fun updateEntity(employee: Employee, updateRequest: EmployeeUpdateRequest): Employee {
        return updateRequest.run {
            employee.apply {
                updateRequest.experience.let { this.experience = it }
                updateRequest.degree.let { this.degree = it }
            }
        }
    }
}


@Component
class ServiceMapper {

    fun toDto(service: Services): ServiceResponse {
        return service.run {
            ServiceResponse(
                id = this.id,
                name = this.name,
                description = this.description,
                price = this.price,
                departmentName = department.name
            )
        }
    }

    fun toEntity(createRequest: ServiceCreateRequest, department: Department): Services {
        return createRequest.run {
            Services(
                name = this.name,
                description = this.description,
                price = this.price,
                department = department
            )
        }
    }

    fun updateEntity(service: Services, updateRequest: ServiceUpdateRequest): Services {
        return updateRequest.run {
            service.apply {
                updateRequest.name.let { this.name = it }
                updateRequest.description.let { this.description = it }
                updateRequest.price.let { this.price = it }
            }
        }
    }
}


@Component
class ScheduleMapper {

    fun toDto(schedule: Schedule): ScheduleResponse {
        return schedule.run {
            ScheduleResponse(
                id = this.id,
                doctorName = this.doctor.user.username,
                dayOfWeek = this.dayOfWeek,
                startTime = this.startTime,
                endTime = this.endTime,
                date = this.date
            )
        }
    }

    fun toEntity(createRequest: ScheduleCreateRequest, doctor: Employee): Schedule {
        return createRequest.run {
            Schedule(
                doctor = doctor,
                dayOfWeek = this.dayOfWeek,
                startTime = this.startTime,
                endTime = this.endTime
            )
        }
    }

    fun updateEntity(schedule: Schedule, updateRequest: ScheduleUpdateRequest): Schedule {
        return updateRequest.run {
            schedule.apply {
                updateRequest.dayOfWeek.let { this.dayOfWeek = it }
                updateRequest.startTime.let { this.startTime = it }
                updateRequest.endTime.let { this.endTime = it }
            }
        }
    }
}

@Component
class TransactionMapper {

    fun toDto(transaction: Transaction): TransactionResponse {
        return transaction.run {
            TransactionResponse(
                id = this.id,
                patientName = this.patient.user.fullName,
                serviceName = this.service.name,
                amount = this.amount,
                paymentMethod = this.paymentMethod,
                doctorName = this.doctor.user.fullName
            )
        }
    }

    fun toEntity(createRequest: TransactionCreateRequest, patient: Patient, service: Services, doctor: Employee): Transaction {
        return createRequest.run {
            Transaction(
                patient = patient,
                service = service,
                amount = this.amount,
                paymentMethod = this.paymentMethod,
                doctor = doctor
            )
        }
    }

    fun updateEntity(transaction: Transaction, updateRequest: TransactionUpdateRequest): Transaction {
        return updateRequest.run {
            transaction.apply {
                updateRequest.amount.let { this.amount = it }
                updateRequest.paymentMethod.let { this.paymentMethod = it }
            }
        }
    }
}

@Component
class AppointmentMapper {

    fun toDto(appointment: Appointment): AppointmentResponse {
        return appointment.run {
            AppointmentResponse(
                id = this.id,
                patientName = this.patient.user.fullName,
                doctorName = this.doctor.user.fullName,
                orderedDate = this.orderedDate,
                orderedTime = this.orderedTime,
                orderStatus = this.orderStatus
            )
        }
    }

    fun toEntity(createRequest: AppointmentCreateRequest, patient: Patient, doctor: Employee): Appointment {
        return createRequest.run {
            Appointment(
                patient = patient,
                doctor = doctor,
                orderedDate = this.orderedDate,
                orderedTime = this.orderedTime
            )
        }
    }

    fun updateEntity(appointment: Appointment, updateRequest: AppointmentUpdateRequest): Appointment {
        return updateRequest.run {
            appointment.apply {
                updateRequest.orderedDate.let { this.orderedDate = it }
                updateRequest.orderedTime.let { this.orderedTime = it }
                updateRequest.orderStatus.let { this.orderStatus = it }
            }
        }
    }
}


@Component
class TestResultMapper {

    fun toDto(testResult: TestResult): TestResultResponse {
        return testResult.run {
            TestResultResponse(
                id = this.id,
                patientName = this.patient.user.fullName,
                serviceName = this.service.name,
                result = this.result,
                doctorName = this.doctor.user.fullName
            )
        }
    }

    fun toEntity(createRequest: TestResultCreateRequest, patient: Patient, service: Services, doctor: Employee): TestResult {
        return createRequest.run {
            TestResult(
                patient = patient,
                service = service,
                result = this.result,
                doctor = doctor
            )
        }
    }

    fun updateEntity(testResult: TestResult, updateRequest: TestResultUpdateRequest): TestResult {
        return updateRequest.run {
            testResult.apply {
                updateRequest.result.let { this.result = it }
            }
        }
    }
}


@Component
class DepartmentMapper {

    fun toDto(department: Department): DepartmentResponse {
        return department.run {
            DepartmentResponse(
                id = this.id,
                name = this.name
            )
        }
    }

    fun toEntity(createRequest: DepartmentCreateRequest): Department {
        return createRequest.run {
            Department(
                name = this.name
            )
        }
    }

    fun updateEntity(department: Department, updateRequest: DepartmentUpdateRequest): Department {
        return updateRequest.run {
            department.apply {
                updateRequest.name.let { this.name = it }
            }
        }
    }
}


@Component
class ClinicMapper {

    fun toDto(clinic: Clinic): ClinicResponse {
        return clinic.run {
            ClinicResponse(
                id = this.id,
                name = this.name,
                address = this.address
            )
        }
    }

    fun toEntity(createRequest: ClinicCreateRequest): Clinic {
        return createRequest.run {
            Clinic(
                name = this.name,
                address = this.address
            )
        }
    }

    fun updateEntity(clinic: Clinic, updateRequest: ClinicUpdateRequest): Clinic {
        return updateRequest.run {
            clinic.apply {
                updateRequest.name.let { this.name = it }
                updateRequest.address.let { this.address = it }
            }
        }
    }
}

