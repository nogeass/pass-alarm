import SwiftUI

enum TutorialStep: Int, CaseIterable {
    case createAlarm = 0
    case skipAlarm = 1
    case deleteAlarm = 2

    var title: String {
        switch self {
        case .createAlarm: return "アラームをセットしてみましょう"
        case .skipAlarm: return "スキップしてみましょう"
        case .deleteAlarm: return "不要なアラームは削除できます"
        }
    }

    var hint: String {
        switch self {
        case .createAlarm: return "下のボタンをタップしてアラームを作成"
        case .skipAlarm: return "右にスワイプしてアラームをパス"
        case .deleteAlarm: return "長押しして「削除」を選択"
        }
    }
}

struct TutorialFlowView: View {
    @Environment(DIContainer.self) private var container
    var onComplete: () -> Void

    @State private var currentStep: TutorialStep = .createAlarm
    @State private var showAlarmEdit = false
    @State private var showToast = false
    @State private var toastMessage = ""
    @State private var refreshId = UUID()

    var body: some View {
        ZStack {
            VStack(spacing: 0) {
                // Coaching banner
                tutorialBanner

                // Content area
                switch currentStep {
                case .createAlarm:
                    createAlarmContent
                case .skipAlarm:
                    skipAlarmContent
                case .deleteAlarm:
                    deleteAlarmContent
                }
            }

            VStack {
                Spacer()
                PraiseToast(message: toastMessage, isVisible: $showToast)
                    .padding(.bottom, PassSpacing.xl)
            }
        }
        .task {
            await inferStep()
        }
        .sheet(isPresented: $showAlarmEdit, onDismiss: {
            Task { await checkAlarmCreated() }
        }) {
            AlarmEditSheet(
                plan: nil,
                onSaved: {
                    showAlarmEdit = false
                }
            )
        }
    }

    // MARK: - Coaching Banner

    private var tutorialBanner: some View {
        VStack(spacing: PassSpacing.sm) {
            Text("\(currentStep.rawValue + 1)/3")
                .font(.caption)
                .fontWeight(.bold)
                .foregroundStyle(.white.opacity(0.6))

            Text(currentStep.title)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(.white)
                .multilineTextAlignment(.center)

            Text(currentStep.hint)
                .font(.subheadline)
                .foregroundStyle(.white.opacity(0.6))
        }
        .padding(.vertical, PassSpacing.lg)
        .padding(.horizontal, PassSpacing.md)
        .frame(maxWidth: .infinity)
        .background(.ultraThinMaterial.opacity(0.3))
    }

    // MARK: - Step 1: Create Alarm

    private var createAlarmContent: some View {
        VStack(spacing: PassSpacing.lg) {
            Spacer()

            Image(systemName: "alarm.fill")
                .font(.system(size: 64))
                .foregroundStyle(.white.opacity(0.6))

            PassButton(
                title: "アラームを作成",
                size: .large,
                color: PassColors.brand,
                haptic: .success
            ) {
                showAlarmEdit = true
            }
            .padding(.horizontal, PassSpacing.xl)

            Spacer()
        }
    }

    // MARK: - Step 2: Skip Alarm

    private var skipAlarmContent: some View {
        SkipQueueView(onSkipPerformed: {
            showPraise("スキップできた！")
            Task {
                try? await Task.sleep(for: .seconds(1))
                withAnimation {
                    currentStep = .deleteAlarm
                    refreshId = UUID()
                }
            }
        })
        .id(refreshId)
    }

    // MARK: - Step 3: Delete Alarm

    private var deleteAlarmContent: some View {
        AlarmListView(
            onEdit: { _ in },
            onDeletePerformed: {
                Task { await completeTutorial() }
            }
        )
        .id(refreshId)
    }

    // MARK: - Actions

    private func inferStep() async {
        let plans = (try? await container.alarmPlanRepository.fetchAll()) ?? []
        if plans.isEmpty {
            currentStep = .createAlarm
            return
        }
        let queue = (try? await container.computeQueueUseCase.execute()) ?? []
        let hasSkipped = queue.contains(where: { $0.isSkipped })
        if !hasSkipped {
            currentStep = .skipAlarm
        } else {
            currentStep = .deleteAlarm
        }
    }

    private func checkAlarmCreated() async {
        let plans = (try? await container.alarmPlanRepository.fetchAll()) ?? []
        if !plans.isEmpty {
            showPraise("いいね！")
            try? await Task.sleep(for: .seconds(1))
            withAnimation {
                currentStep = .skipAlarm
                refreshId = UUID()
            }
        }
    }

    private func completeTutorial() async {
        showPraise("準備完了！")
        var settings = await container.appSettingsRepository.get()
        settings.tutorialCompleted = true
        await container.appSettingsRepository.save(settings)
        try? await Task.sleep(for: .seconds(1.5))
        onComplete()
    }

    private func showPraise(_ message: String) {
        toastMessage = message
        withAnimation(.spring(response: 0.3)) {
            showToast = true
        }
    }
}
