import SwiftUI

struct AlarmEditSheet: View {
    let plan: AlarmPlan?
    var onSaved: () -> Void

    @Environment(DIContainer.self) private var container
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel: AlarmEditViewModel?
    @State private var showToast = false
    @State private var toastMessage = ""
    @State private var showSoundPicker = false

    var body: some View {
        ZStack {
            MapBackdrop(timeOfDay: .evening)

            Group {
                if let vm = viewModel {
                    editContent(vm)
                } else {
                    ProgressView()
                        .tint(.white)
                }
            }

            // Close button
            VStack {
                HStack {
                    Spacer()
                    Button {
                        PassHaptics.tap()
                        dismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 28))
                            .foregroundStyle(.white.opacity(0.6))
                    }
                }
                .padding(.horizontal, PassSpacing.lg)
                .padding(.top, PassSpacing.md)

                Spacer()
            }

            VStack {
                Spacer()
                PraiseToast(message: toastMessage, isVisible: $showToast)
                    .padding(.bottom, PassSpacing.xl)
            }
        }
        .task {
            if viewModel == nil {
                viewModel = AlarmEditViewModel(container: container, plan: plan)
            }
        }
    }

    @ViewBuilder
    private func editContent(_ vm: AlarmEditViewModel) -> some View {
        ScrollView {
            VStack(spacing: PassSpacing.lg) {
                Spacer()
                    .frame(height: PassSpacing.xxl)

                // Title
                Text(vm.isEditing ? "アラーム編集" : "アラーム追加")
                    .font(.system(size: 22, weight: .bold, design: .rounded))
                    .foregroundStyle(.white)

                // Time picker (10-minute intervals)
                VStack(spacing: PassSpacing.sm) {
                    Text("時刻")
                        .font(PassTypography.sectionHeader)
                        .foregroundStyle(.white.opacity(0.6))
                        .frame(maxWidth: .infinity, alignment: .leading)

                    HStack(spacing: 0) {
                        Picker("時", selection: Binding(
                            get: { vm.selectedHour },
                            set: { vm.selectedHour = $0 }
                        )) {
                            ForEach(0..<24, id: \.self) { h in
                                Text(String(format: "%02d", h))
                                    .foregroundStyle(.white)
                                    .tag(h)
                            }
                        }
                        .pickerStyle(.wheel)
                        .frame(maxWidth: .infinity)

                        Text(":")
                            .font(.system(size: 24, weight: .bold))
                            .foregroundStyle(.white)

                        Picker("分", selection: Binding(
                            get: { vm.selectedMinute },
                            set: { vm.selectedMinute = $0 }
                        )) {
                            ForEach(AlarmEditViewModel.minuteOptions, id: \.self) { m in
                                Text(String(format: "%02d", m))
                                    .foregroundStyle(.white)
                                    .tag(m)
                            }
                        }
                        .pickerStyle(.wheel)
                        .frame(maxWidth: .infinity)
                    }
                    .frame(height: 150)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                }

                // Label
                VStack(spacing: PassSpacing.sm) {
                    Text("ラベル")
                        .font(PassTypography.sectionHeader)
                        .foregroundStyle(.white.opacity(0.6))
                        .frame(maxWidth: .infinity, alignment: .leading)

                    TextField("ラベル", text: Binding(
                        get: { vm.label },
                        set: { vm.label = $0 }
                    ))
                    .textFieldStyle(.plain)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundStyle(.white)
                    .padding(PassSpacing.md)
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

                // Sound picker
                VStack(spacing: PassSpacing.sm) {
                    Text("アラーム音")
                        .font(PassTypography.sectionHeader)
                        .foregroundStyle(.white.opacity(0.6))
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Button {
                        PassHaptics.tap()
                        showSoundPicker = true
                    } label: {
                        HStack {
                            Text(soundLabel(for: vm.soundId))
                                .foregroundStyle(.white)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundStyle(.white.opacity(0.4))
                        }
                        .padding(PassSpacing.md)
                        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: PassSpacing.cardCorner))
                    }
                }
                .sheet(isPresented: $showSoundPicker) {
                    SystemSoundPicker(selectedSoundId: Binding(
                        get: { vm.soundId },
                        set: { vm.soundId = $0 }
                    ))
                    .presentationDetents([.medium])
                }

                // Limit error
                if vm.showLimitError {
                    Text("アラームの上限に達しました。Proにアップグレードしてください。")
                        .font(.caption)
                        .foregroundStyle(PassColors.stopRed)
                        .padding(.horizontal, PassSpacing.sm)
                }

                // Save button
                PassButton(
                    title: "保存",
                    size: .large,
                    color: PassColors.brand,
                    isEnabled: vm.weekdaysMask > 0,
                    haptic: .success
                ) {
                    Task {
                        let success = await vm.save()
                        if success {
                            onSaved()
                        }
                    }
                }

                // Delete button (edit mode only)
                if vm.isEditing {
                    PassButton(
                        title: "削除",
                        size: .medium,
                        color: PassColors.stopRed,
                        haptic: .medium
                    ) {
                        Task {
                            await vm.delete()
                            dismiss()
                        }
                    }
                }

                Spacer()
                    .frame(height: PassSpacing.xl)
            }
            .padding(.horizontal, PassSpacing.md)
        }
    }

    private func soundLabel(for id: String) -> String {
        let labels: [String: String] = [
            "default": "デフォルト",
            "alarm": "アラーム",
            "beacon": "ビーコン",
            "bulletin": "ブルティン",
            "radar": "レーダー",
            "signal": "シグナル",
        ]
        return labels[id] ?? id
    }
}
