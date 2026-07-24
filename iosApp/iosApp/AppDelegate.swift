//
// Created by Derrick rocha on 7/23/26.
//

import BackgroundTasks
import Shared  // Your KMP Shared Framework Name
import SwiftUI

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // 1. Register handlers instantly (Prevents the crash)
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "tenants",
            using: nil
        ) { task in
            IosTaskManager.shared.executeBackgroundTask(
                taskId: "tenants",
                task: task as! BGAppRefreshTask
            )
        }

        // 2. Initialize Kotlin worker registry registry ONLY (No scheduling yet!)
        Dependencies.shared.setupBackgroundTasks()


        return true
    }
}
