import SwiftUI

struct QueueView: View {
    @Environment(DIContainer.self) private var container
    @State private var viewModel: QueueViewModel?
    @State private var showToast = false
    @State private var toastMessage = ""

    var body: some View {
        NavigationStack {
            ZStack {
                MapBackdrop(timeOfDay: .noon)

                Group {
                    if let vm = viewModel {
                        queueContent(vm)
                    } else {
                        ProgressView()
                    }
                }

                VStack {
                    Spacer()
                    PraiseToast(message: toastMessage, isVisible: $showToast)
                        .padding(.bottom, PassSpacing.xl)
                }
            }
            .navigationTitle("キュー")
            .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        }
        .task {
            if viewModel == nil {
                let vm = QueueViewModel(container: container)
                viewModel = vm
                await vm.load()
            }
        }
        .fullScreenCover(isPresented: Binding(
            get: { viewModel?.showProPurchase ?? false },
            set: { viewModel?.showProPurchase = $0 }
        )) {
            if let vm = viewModel {
                ProPurchaseView(
                    onPurchased: {
                        vm.showProPurchase = false
                        vm.isPro = true
                        toastMessage = PraiseMessages.randomPurchase()
                        withAnimation(.spring(response: 0.3)) {
                            showToast = true
                        }
                    },
                    onDismiss: {
                        vm.showProPurchase = false
                    }
                )
            }
        }
    }

    @ViewBuilder
    private func queueContent(_ vm: QueueViewModel) -> some View {
        if vm.queue.isEmpty {
            VStack(spacing: PassSpacing.md) {
                Image(systemName: "alarm")
                    .font(.system(size: 48))
                    .foregroundStyle(.white.opacity(0.4))
                Text("アラームがありません")
                    .font(.headline)
                    .foregroundStyle(.white.opacity(0.6))
                Text("アラームを設定してください")
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.4))
            }
        } else {
            ScrollView {
                // Fixed: Next alarm header
                if let next = vm.queue.first(where: { !$0.isSkipped }) {
                    HStack {
                        Text("次に鳴る")
                            .font(PassTypography.sectionHeader)
                            .foregroundStyle(.white.opacity(0.6))
                        Spacer()
                        Text("\(next.date) \(next.timeHHmm)")
                            .font(PassTypography.cardDate)
                            .foregroundStyle(.white)
                    }
                    .padding(.horizontal, PassSpacing.md)
                    .padding(.top, PassSpacing.md)
                }

                LazyVStack(spacing: PassSpacing.sm) {
                    ForEach(vm.queue) { occurrence in
                        AlarmCardView(
                            occurrence: occurrence,
                            onSkip: {
                                Task {
                                    await vm.skip(date: occurrence.date)
                                    if let msg = PraiseMessages.randomSkip() {
                                        toastMessage = msg
                                        withAnimation(.spring(response: 0.3)) {
                                            showToast = true
                                        }
                                    }
                                }
                            },
                            onUnskip: {
                                Task { await vm.unskip(date: occurrence.date) }
                            }
                        )
                    }

                    if !vm.isPro && !vm.queue.isEmpty {
                        ProCardView {
                            vm.showProPurchase = true
                        }
                    }
                }
                .padding(.horizontal, PassSpacing.md)
                .padding(.bottom, PassSpacing.xl)
            }
        }
    }
}
