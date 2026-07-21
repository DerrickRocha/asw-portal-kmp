package org.example.asw_portal_kmp.data.schedulers

class IosBackgroundScheduler: BackgroundScheduler {
    override fun schedulePeriodicTask(
        taskId: String,
        workerName: String,
        intervalMs: Long,
        constraints: BackgroundConstraints
    ) {
        TODO("Not yet implemented")
    }

    override fun scheduleOneTimeTask(
        taskId: String,
        workerName: String,
        delayMs: Long,
        constraints: BackgroundConstraints
    ) {
        TODO("Not yet implemented")
    }

    override fun cancelTask(taskId: String) {
        TODO("Not yet implemented")
    }
}