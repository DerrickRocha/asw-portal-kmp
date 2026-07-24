import SwiftUI
import BackgroundTasks
import Shared

@main
struct iOSApp: App {

    init() {
        // 1. CRITICAL: Register FIRST using a raw string so it's instantaneous
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "tenants", // Hardcoded to match Info.plist exactly
            using: nil
        ) { task in
            // Forward down to your KMP execution coordinator
            IosTaskManager.shared.executeBackgroundTask(
                taskId: "tenants",
                task: task as! BGAppRefreshTask
            )
        }

        // 2. Initialize your Kotlin KMP Dependencies AFTER registration
        Dependencies.shared.setupBackgroundTasks()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
