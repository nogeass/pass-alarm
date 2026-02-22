import SwiftUI

struct AlarmSettingsView: View {
    @Environment(DIContainer.self) private var container
    @State private var viewModel: AlarmSettingsViewModel?
    @State private var showToast = false
    @State private var toastMessage = ""

    var body: some View {
        NavigationStack {
            ZStack {
                MapBackdrop(timeOfDay: .evening)

                Group {
                    if let vm = viewModel {
                        settingsContent(vm)
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
            .navigationTitle("アラーム設定")
            .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        }
        .task {
            if viewModel == nil {
                let vm = AlarmSettingsViewModel(container: container)
                viewModel = vm
                await vm.load()
            }
        }
    }

    @ViewBuilder
    private func settingsContent(_ vm: AlarmSettingsViewModel) -> some View {
        ScrollView {
            VStack(spacing: PassSpacing.lg) {
                // Time picker - standard for reliability
                VStack(spacing: PassSpacing.sm) {
                    Text("時刻")
                        .font(PassTypography.sectionHeader)
                        .foregroundStyle(.white.opacity(0.6))
                        .frame(maxWidth: .infinity, alignment: .leading)

                    DatePicker("", selection: Binding(
                        get: { vm.selectedTime },
                        set: { vm.selectedTime = $0 }
                    ), displayedComponents: .hourAndMinute)
                    .datePickerStyle(.wheel)
                    .labelsHidden()
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                }

                // Weekday selector
                VStack(spacing: PassSpacing.sm) {
                    Text("曜日")
                        .font(PassTypography.sectionHeader)
                        .foregroundStyle(.white.opacity(0.6))
                        .frame(maxWidth: .infinity, alignment: .leading)

                    HStack(spacing: PassSpacing.sm) {
                        ForEach(WeekdaySet.allDays, id: \.2) { day, label, _ in
                            let isSelected = WeekdaySet(rawValue: vm.weekdaysMask).contains(day)
                            Button {
                                PassHaptics.tap()
                                if isSelected {
                                    vm.weekdaysMask &= ~day.rawValue
                                } else {
                                    vm.weekdaysMask |= day.rawValue
                                }
                            } label: {
                                Text(label)
                                    .font(.system(size: 14, weight: .bold))
                                    .frame(width: 40, height: 40)
                                    .foregroundStyle(isSelected ? .white : .white.opacity(0.5))
                                    .background(
                                        Circle()
                                            .fill(isSelected ? PassColors.brand : Color.white.opacity(0.1))
                                    )
                            }
                        }
                    }
                    .padding(PassSpacing.md)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                }

                // Repeat settings
                VStack(spacing: PassSpacing.md) {
                    Text("連続アラーム")
                        .font(PassTypography.sectionHeader)
                        .foregroundStyle(.white.opacity(0.6))
                        .frame(maxWidth: .infinity, alignment: .leading)

                    VStack(spacing: PassSpacing.md) {
                        HStack {
                            Text("回数")
                                .foregroundStyle(.white)
                            Spacer()
                            Stepper("\(vm.repeatCount)回", value: Binding(
                                get: { vm.repeatCount },
                                set: { vm.repeatCount = $0 }
                            ), in: 1...20)
                            .foregroundStyle(.white)
                        }

                        HStack {
                            Text("間隔")
                                .foregroundStyle(.white)
                            Spacer()
                            Stepper("\(vm.intervalMin)分", value: Binding(
                                get: { vm.intervalMin },
                                set: { vm.intervalMin = $0 }
                            ), in: 1...30)
                            .foregroundStyle(.white)
                        }
                    }
                    .padding(PassSpacing.md)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                }

                // Holiday
                VStack(spacing: PassSpacing.sm) {
                    HStack {
                        VStack(alignment: .leading) {
                            Text("祝日を自動スキップ")
                                .foregroundStyle(.white)
                            Text("日本の祝日は自動でパスします")
                                .font(.caption)
                                .foregroundStyle(.white.opacity(0.5))
                        }
                        Spacer()
                        Toggle("", isOn: Binding(
                            get: { vm.holidayAutoSkip },
                            set: { vm.holidayAutoSkip = $0 }
                        ))
                        .labelsHidden()
                    }
                    .padding(PassSpacing.md)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                }

                // Save
                PassButton(
                    title: "保存",
                    size: .large,
                    color: PassColors.brand,
                    haptic: .success
                ) {
                    Task {
                        await vm.save()
                        toastMessage = PraiseMessages.randomSettingsComplete()
                        withAnimation(.spring(response: 0.3)) {
                            showToast = true
                        }
                    }
                }
            }
            .padding(PassSpacing.md)
        }
    }
}
