package zeroone.developers.clinicapp.security

import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import zeroone.developers.clinicapp.*

@Component
class ContextRefreshEvent(
    private val userRepository: UserRepository,
    private val employeeRepository: EmployeeRepository,
    private val clinicRepository: ClinicRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @EventListener(ContextRefreshedEvent::class)
    fun contextRefreshedEvent() {
        val clinic = clinicRepository.findFirst().orElseGet {
            clinicRepository.save(Clinic(name = "Main Clinic", address = "123 Main St"))
        }

        createEmployeeIfNotExists("cashier", "cashier123", Role.CASHIER, clinic)
        createEmployeeIfNotExists("director", "director123", Role.DIRECTOR, clinic)
        createEmployeeIfNotExists("dev", "dev123", Role.DEV, clinic)
    }

    private fun createEmployeeIfNotExists(username: String, password: String, role: Role, clinic: Clinic) {
        if (userRepository.findByUsernameAndDeletedFalse(username) == null) {
            val user = User(
                username = username,
                password = passwordEncoder.encode(password),
                fullName = "${role.name} User",
                phoneNumber = "123456789",
                address = "Company",
                gender = Gender.MALE
            )
            val savedUser = userRepository.save(user)

            val employee = Employee(
                experience = 5L,
                degree = "Bachelor",
                user = savedUser,
                role = role,
                clinic = clinic
            )
            employeeRepository.save(employee)

            println("$role User yaratildi: $username")
        }
    }
}

