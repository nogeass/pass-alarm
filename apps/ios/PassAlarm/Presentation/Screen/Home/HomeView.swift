import SwiftUI

struct HomeView: View {
    @Environment(DIContainer.self) private var container
    @State private var viewModel: HomeViewModel?
    @State private var showToast = false
    @State private var toastMessage = ""

    var body: some View {
        NavigationStack {
            ZStack {
                MapBackdrop(timeOfDay: .morning)

                Group {
                    if let vm = viewModel {
                        homeContent(vm)
                    } else {
                        ProgressView()
                    }
                }

                // PraiseToast overlay
                VStack {
                    Spacer()
                    PraiseToast(message: toastMessage, isVisible: $showToast)
                        .padding(.bottom, PassSpacing.xl)
                }
            }
            .navigationTitle("パスアラーム")
            .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        }
        .task {
            if viewModel == nil {
                let vm = HomeViewModel(container: container)
                viewModel = vm
                await vm.load()
            }
        }
    }

    @ViewBuilder
    private func homeContent(_ vm: HomeViewModel) -> some View {
        VStack(spacing: PassSpacing.lg) {
            Spacer()

            if let next = vm.nextOccurrence {
                NextAlarmCard(occurrence: next)
            } else {
                Text("次のアラームはありません")
                    .font(PassTypography.cardDate)
                    .foregroundStyle(.white.opacity(0.7))
                    .padding(PassSpacing.xl)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
            }

            if let plan = vm.plan {
                HStack {
                    Text("アラーム")
                        .font(.headline)
                        .foregroundStyle(.white)
                    Spacer()
                    PassToggle(isOn: Binding(
                        get: { plan.isEnabled },
                        set: { newValue in
                            Task { await vm.togglePlan(newValue) }
                        }
                    ))
                }
                .padding(.horizontal, PassSpacing.lg)
            }

            PassButton(
                title: "今日だけパス",
                size: .large,
                color: PassColors.skipOrange,
                isEnabled: vm.nextOccurrence != nil,
                haptic: .medium
            ) {
                Task {
                    await vm.skipToday()
                    if let msg = PraiseMessages.randomSkip() {
                        toastMessage = msg
                        withAnimation(.spring(response: 0.3)) {
                            showToast = true
                        }
                    }
                }
            }
            .padding(.horizontal, PassSpacing.lg)

            // Swipe hint to Queue
            HStack(spacing: PassSpacing.xs) {
                Text("キューを見る")
                    .font(.caption)
                    .foregroundStyle(.white.opacity(0.5))
                Image(systemName: "chevron.right")
                    .font(.caption2)
                    .foregroundStyle(.white.opacity(0.3))
            }

            Spacer()
        }
        .padding()
    }
}
