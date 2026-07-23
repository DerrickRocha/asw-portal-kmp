import SwiftUI
import BackgroundTasks
import Shared

@main
struct iOSApp: App {

    init() {
        setupBackgroundTaskHandlers()
        Dependencies.shared.setupBackgroundTasks()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private func setupBackgroundTaskHandlers() {

        let identifiers = Dependencies.shared.workerNames
        for identifier in identifiers {
            BGTaskScheduler.shared.register(
                forTaskWithIdentifier: identifier,
                using: nil
            ) { task in
                // This runs when the background task is triggered
                IosTaskManager.shared.executeBackgroundTask(
                    taskId: identifier,
                    task: task as! BGTask
                )
            }
        }
    }
}
