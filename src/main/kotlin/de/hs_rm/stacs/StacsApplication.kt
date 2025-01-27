package de.hs_rm.stacs

import de.hs_rm.stacs.service.NlpPipelineService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StacsApplication

fun main(args: Array<String>) {
	runApplication<StacsApplication>(*args)
}

