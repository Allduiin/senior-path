package com.seniorpath.outbox

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

class SimulatedCrashException(label: String) :
    RuntimeException("simulated process crash at [$label]")

@Component
class CrashPoint {

    private val armed = ConcurrentHashMap.newKeySet<String>()

    fun arm(label: String) {
        armed.add(label)
    }

    fun maybeCrash(label: String) {
        if (armed.remove(label)) {
            throw SimulatedCrashException(label)
        }
    }

    companion object {
        const val AFTER_COMMIT_BEFORE_PUBLISH = "after-commit-before-publish"
    }
}
