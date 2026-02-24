import SwiftUI

struct AlarmListView: View {
    @Environment(DIContainer.self) private var container
    @Environment(\.scenePhase) private var scenePhase
    @State private var viewModel: AlarmListViewModel?
    @State private var showPermissionAlert = false
    @State private var pendingTogglePlanId: UUID?
    var onEdit: (AlarmPlan) -> Void
    var onDeletePerformed: (() -> Void)? = nil

    var body: some View {
        Group {
            if let vm = viewModel {
                if vm.plans.isEmpty {
                    emptyState
                } else {
                    listContent(vm)
                }
            } else {
                ProgressView()
                    .tint(.white)
            }
        }
        .task {
            if viewModel == nil {
                let vm = AlarmListViewModel(container: container)
                viewModel = vm
                await vm.load()
            }
        }
        .alert("通知が必要です", isPresented: $showPermissionAlert) {
            Button("設定を開く") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            Button("キャンセル", role: .cancel) {
                pendingTogglePlanId = nil
            }
        } message: {
            Text("通知がOFFだとアラームが鳴りません。設定アプリで通知を許可してください。")
        }
        .onChange(of: scenePhase) { _, newPhase in
            guard newPhase == .active, let planId = pendingTogglePlanId else { return }
            Task {
                let status = await container.notificationPermission.currentStatus()
                if status == .authorized {
                    await viewModel?.togglePlan(planId, isEnabled: true)
                    pendingTogglePlanId = nil
                } else {
                    showPermissionAlert = true
                }
            }
        }
    }

    private var emptyState: some View {
        VStack(spacing: PassSpacing.md) {
            Spacer()

            Image(systemName: "alarm")
                .font(.system(size: 48))
                .foregroundStyle(.white.opacity(0.4))

            Text("アラームを追加しよう")
                .font(.headline)
                .foregroundStyle(.white.opacity(0.6))

            Text("+ ボタンで新規作成")
                .font(.subheadline)
                .foregroundStyle(.white.opacity(0.4))

            Spacer()
        }
    }

    @ViewBuilder
    private func listContent(_ vm: AlarmListViewModel) -> some View {
        ScrollView {
            LazyVStack(spacing: PassSpacing.sm) {
                ForEach(vm.plans) { plan in
                    AlarmRowView(
                        plan: plan,
                        onToggle: { isEnabled, revert in
                            if !isEnabled {
                                Task { await vm.togglePlan(plan.id, isEnabled: false) }
                                return
                            }
                            Task {
                                let status = await container.notificationPermission.currentStatus()
                                switch status {
                                case .authorized:
                                    await vm.togglePlan(plan.id, isEnabled: true)
                                case .notDetermined:
                                    let granted = (try? await container.notificationPermission.request()) ?? false
                                    if granted {
                                        await vm.togglePlan(plan.id, isEnabled: true)
                                    } else {
                                        revert()
                                    }
                                case .denied, .provisional:
                                    revert()
                                    pendingTogglePlanId = plan.id
                                    showPermissionAlert = true
                                }
                            }
                        },
                        onTap: {
                            onEdit(plan)
                        }
                    )
                    .contextMenu {
                        Button(role: .destructive) {
                            Task {
                                await vm.deletePlan(plan.id)
                                onDeletePerformed?()
                            }
                        } label: {
                            Label("削除", systemImage: "trash")
                        }
                    }
                }
            }
            .padding(.horizontal, PassSpacing.md)
            .padding(.top, PassSpacing.sm)
            .padding(.bottom, 100) // Space for floating pill
        }
    }

    /// Called by parent to refresh the list after changes
    func refresh() async {
        await viewModel?.load()
    }
}
