import shared
import SwiftUI

class AppDelegate: NSObject, UIApplicationDelegate {
    private var stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: nil)
    var backDispatcher: BackDispatcher = BackDispatcherKt.BackDispatcher()
    
    lazy var root: Root = {
        CreateRootComponentSafeKt.createRootComponentSafe(lifecycle: ApplicationLifecycle(),stateKeeper: stateKeeper, backDispatcher: backDispatcher, onDestroy: {}) {_ in
            StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: nil)
        }
    }()
    
    func application(_ application: UIApplication, shouldSaveSecureApplicationState coder: NSCoder) -> Bool {
        
        StateKeeperUtilsKt.save(coder: coder, state: stateKeeper.save())
        
        return true
    }
    
    func application(_ application: UIApplication, shouldRestoreSecureApplicationState coder: NSCoder) -> Bool {
        stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: StateKeeperUtilsKt.restore(coder: coder))
        return true
    }
}
