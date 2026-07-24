import BackgroundTasks
import Shared
import SwiftUI

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onAppear {
                    // Safe to schedule now that the app is active and visible
                    Dependencies.shared.scheduler.schedulePeriodicTask(
                        taskId: "tenants",
                        workerName: "tenants",
                        intervalMs: 15 * 60 * 1000,  // 15 minutes,
                        constraints: BackgroundConstraints(
                            requiresNetwork: true,
                            requiresCharging: false,
                            requiresDeviceIdle: false,
                            requiresStorageNotLow: false,
                            requiresUnmeteredNetwork: false
                        )
                    )
                }
        }
    }
}
