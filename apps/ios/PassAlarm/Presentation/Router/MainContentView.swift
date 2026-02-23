import SwiftUI

struct MainContentView: View {
    @Environment(DIContainer.self) private var container
    @State private var selectedTab: ContentTab = .list
    @State private var showSettings = false
    @State private var showAlarmEdit = false
    @State private var editingPlan: AlarmPlan? = nil
    @State private var refreshId = UUID()

    var body: some View {
        ZStack {
            // Background gradient changes based on selected tab
            MapBackdrop(timeOfDay: selectedTab == .list ? .morning : .noon)
                .animation(.easeInOut(duration: 0.5), value: selectedTab)

            VStack(spacing: 0) {
                // Large title — left aligned
                Text(selectedTab == .list ? "アラーム一覧" : "次のアラーム")
                    .font(.system(size: 34, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, PassSpacing.md)
                    .padding(.top, PassSpacing.sm)
                    .padding(.bottom, PassSpacing.xs)

                // Content
                switch selectedTab {
                case .list:
                    AlarmListView(onEdit: { plan in
                        editingPlan = plan
                        showAlarmEdit = true
                    })
                    .id(refreshId)

                case .skip:
                    SkipQueueView()
                        .id(refreshId)
                }
            }

            // Bottom floating buttons — tight cluster
            VStack {
                Spacer()
                HStack(spacing: 16) {
                    // Settings button — left
                    Button {
                        PassHaptics.tap()
                        showSettings = true
                    } label: {
                        Image(systemName: "gearshape.fill")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundStyle(.white)
                            .frame(width: 48, height: 48)
                            .background(
                                Circle()
                                    .fill(.ultraThinMaterial)
                            )
                            .clipShape(Circle())
                            .shadow(color: .black.opacity(0.2), radius: 8, y: 2)
                    }
                    .buttonStyle(.plain)

                    // Mode toggle — center
                    ModeToggleFAB(selectedTab: $selectedTab)

                    // Add button — right
                    Button {
                        PassHaptics.tap()
                        editingPlan = nil
                        showAlarmEdit = true
                    } label: {
                        Image(systemName: "plus")
                            .font(.system(size: 24, weight: .black))
                            .foregroundStyle(.white)
                            .frame(width: 48, height: 48)
                            .background(
                                Circle()
                                    .fill(.ultraThinMaterial)
                            )
                            .clipShape(Circle())
                            .shadow(color: .black.opacity(0.2), radius: 8, y: 2)
                    }
                    .buttonStyle(.plain)
                }
                .padding(.bottom, PassSpacing.xl)
            }
        }
        .sheet(isPresented: $showAlarmEdit, onDismiss: {
            refreshId = UUID()
        }) {
            AlarmEditSheet(
                plan: editingPlan,
                onSaved: {
                    showAlarmEdit = false
                }
            )
        }
        .sheet(isPresented: $showSettings) {
            GlobalSettingsSheet()
        }
    }
}
