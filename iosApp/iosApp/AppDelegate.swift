//
// Created by Derrick rocha on 7/23/26.
//

import BackgroundTasks
import Shared  // Your KMP Shared Framework Name
import SwiftUI

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        <#code#>
        // Initialize Multiplatform Dependencies First
        Dependencies.shared.setupBackgroundTasks()

        // Register tasks instantly on launch
        let identifiers = Dependencies.shared.workerNames
        for identifier in identifiers {
            BGTaskScheduler.shared.register(
                forTaskWithIdentifier: identifier,
                using: nil
            ) { task in
                // Safely forward down to our Kotlin executor bridge
                IosTaskManager.shared.executeBackgroundTask(
                    taskId: identifier,
                    task: task as! BGAppRefreshTask
                )
            }
        }
        return true
    }
}
