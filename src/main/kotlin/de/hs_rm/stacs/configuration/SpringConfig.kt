package de.hs_rm.stacs.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Only really needed to enable Scheduling. That way we can fetch new Posts in an interval
 */
@Configuration
@EnableScheduling
class SpringConfig {
}