package com.seniorpath.outbox

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

class SimulatedCrashException(label: String) :
    RuntimeException("simulated process crash at [$label]")

@Component
class CrashPoint {

    private val armed = ConcurrentHashMap.newKeySet<String>()
    private val armedFor = ConcurrentHashMap<String, MutableSet<String>>()

    fun arm(label: String) {
        armed.add(label)
    }

    fun arm(label: String, whenPayloadContains: String) {
        armedFor.computeIfAbsent(label) { ConcurrentHashMap.newKeySet() }.add(whenPayloadContains)
    }

    fun maybeCrash(label: String) {
        if (armed.remove(label)) {
            throw SimulatedCrashException(label)
        }
    }

    fun maybeCrash(label: String, payload: String) {
        val discriminators = armedFor[label] ?: return
        val hit = discriminators.firstOrNull { payload.contains(it) } ?: return
        if (discriminators.remove(hit)) {
            throw SimulatedCrashException("$label:$hit")
        }
    }

    companion object {
        const val AFTER_COMMIT_BEFORE_PUBLISH = "after-commit-before-publish"
        const val AFTER_PUBLISH_BEFORE_MARK = "after-publish-before-mark"
        const val AFTER_CLAIM_BEFORE_EFFECT = "after-claim-before-effect"
    }
}
