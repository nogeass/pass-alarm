import SwiftUI

struct SkipQueueView: View {
    @Environment(DIContainer.self) private var container
    @State private var viewModel: SkipQueueViewModel?
    @State private var showToast = false
    @State private var toastMessage = ""

    var body: some View {
        ZStack {
            Group {
                if let vm = viewModel {
                    skipContent(vm)
                } else {
                    ProgressView()
                        .tint(.white)
                }
            }

            VStack {
                Spacer()
                PraiseToast(message: toastMessage, isVisible: $showToast)
                    .padding(.bottom, 100)
            }
        }
        .task {
            if viewModel == nil {
                let vm = SkipQueueViewModel(container: container)
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
    private func skipContent(_ vm: SkipQueueViewModel) -> some View {
        if vm.queue.isEmpty {
            VStack(spacing: PassSpacing.md) {
                Spacer()

                Image(systemName: "calendar.badge.clock")
                    .font(.system(size: 48))
                    .foregroundStyle(.white.opacity(0.4))

                Text("予定されたアラームがありません")
                    .font(.headline)
                    .foregroundStyle(.white.opacity(0.6))

                Text("アラームを追加して有効にしてください")
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.4))

                Spacer()
            }
        } else {
            VStack(spacing: 0) {
                // Next alarm header
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
                    .padding(.vertical, PassSpacing.sm)
                }

                // Scrollable list with swipe actions
                List {
                    ForEach(vm.queue) { occurrence in
                        AlarmCardView(occurrence: occurrence)
                            .swipeActions(edge: .trailing) {
                                if !occurrence.isSkipped {
                                    Button {
                                        PassHaptics.medium()
                                        Task {
                                            await vm.skip(planId: occurrence.planId, date: occurrence.date)
                                            if let msg = PraiseMessages.randomSkip() {
                                                toastMessage = msg
                                                withAnimation(.spring(response: 0.3)) {
                                                    showToast = true
                                                }
                                            }
                                        }
                                    } label: {
                                        Label("パス", systemImage: "hand.raised.fill")
                                    }
                                    .tint(PassColors.skipOrange)
                                }
                            }
                            .swipeActions(edge: .leading) {
                                if occurrence.isSkipped {
                                    Button {
                                        PassHaptics.tap()
                                        Task {
                                            await vm.unskip(planId: occurrence.planId, date: occurrence.date)
                                        }
                                    } label: {
                                        Label("戻す", systemImage: "arrow.uturn.backward")
                                    }
                                    .tint(PassColors.brand)
                                }
                            }
                            .listRowBackground(Color.clear)
                            .listRowSeparator(.hidden)
                            .listRowInsets(EdgeInsets(
                                top: PassSpacing.xs,
                                leading: PassSpacing.md,
                                bottom: PassSpacing.xs,
                                trailing: PassSpacing.md
                            ))
                    }

                    if !vm.isPro && !vm.queue.isEmpty {
                        ProCardView {
                            vm.showProPurchase = true
                        }
                        .listRowBackground(Color.clear)
                        .listRowSeparator(.hidden)
                        .listRowInsets(EdgeInsets(
                            top: PassSpacing.xs,
                            leading: PassSpacing.md,
                            bottom: PassSpacing.xs,
                            trailing: PassSpacing.md
                        ))
                    }

                    // Bottom spacer for floating buttons
                    Color.clear
                        .frame(height: 80)
                        .listRowBackground(Color.clear)
                        .listRowSeparator(.hidden)
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
            }
        }
    }
}
