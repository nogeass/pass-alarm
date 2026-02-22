import SwiftUI

struct MainTabView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("ホーム", systemImage: "house")
                }

            QueueView()
                .tabItem {
                    Label("キュー", systemImage: "list.bullet")
                }

            AlarmSettingsView()
                .tabItem {
                    Label("設定", systemImage: "gear")
                }
        }
    }
}
