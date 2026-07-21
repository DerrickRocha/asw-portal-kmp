package org.example.asw_portal_kmp.data.schedulers

interface BackgroundScheduler {

    fun schedulePeriodicTask(
        taskId: String,
        workerName: String,
        intervalMs: Long,
        constraints: BackgroundConstraints = BackgroundConstraints()
    )

    fun scheduleOneTimeTask(
        taskId: String,
        workerName: String,
        delayMs: Long = 0,
        constraints: BackgroundConstraints = BackgroundConstraints()
    )

    fun cancelTask(taskId: String)
}