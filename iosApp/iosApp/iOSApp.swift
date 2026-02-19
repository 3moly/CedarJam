import SwiftUI
import shared

@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self)
     var appDelegate
     
    
    init(){
        
        InitAppKt.doInitApp(context: DomainAndroidApplicationContext(), isRelease: true, isTest: false)
    }
    
    var fixedPreferredContentSizeCategory: UIContentSizeCategory {
           return .small
       }
      
    var body: some Scene {
        WindowGroup {
            
            let container = ZStack{
                ContentView(root: appDelegate.root, backDispatcher: appDelegate.backDispatcher)
                 
            }
            
            if #available(iOS 15, *) {
                container.dynamicTypeSize(.large ... .large)
            } else {
                container
            }
        }
    }
}
