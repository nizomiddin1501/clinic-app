package zeroone.developers.clinicapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
class ClinicAppApplication

fun main(args: Array<String>) {
    runApplication<ClinicAppApplication>(*args)
}
