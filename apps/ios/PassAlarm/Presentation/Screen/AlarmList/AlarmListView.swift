import SwiftUI

struct AlarmListView: View {
    @Environment(DIContainer.self) private var container
    @State private var viewModel: AlarmListViewModel?
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

            Text("右上の + ボタンで新規作成")
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
                        onToggle: { isEnabled in
                            Task { await vm.togglePlan(plan.id, isEnabled: isEnabled) }
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
