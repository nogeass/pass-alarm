import AudioToolbox
import SwiftUI

/// Displays a list of preset alarm sounds for user selection.
///
/// Tapping a row plays a short preview via `AudioServicesPlaySystemSound`.
/// The picker uses well-known UISounds file names that are available on
/// real devices at `/System/Library/Audio/UISounds/`.
struct SystemSoundPicker: View {
    @Binding var selectedSoundId: String
    @Environment(\.dismiss) private var dismiss

    /// Preset alarm sounds available on iOS.
    private static let presets: [(id: String, label: String, soundID: SystemSoundID)] = {
        let entries: [(String, String, UInt32)] = [
            ("default", "デフォルト", 1005),
            ("alarm", "アラーム", 1304),
            ("beacon", "ビーコン", 1306),
            ("bulletin", "ブルティン", 1307),
            ("radar", "レーダー", 1308),
            ("signal", "シグナル", 1312),
        ]
        return entries.map { (id: $0.0, label: $0.1, soundID: SystemSoundID($0.2)) }
    }()

    var body: some View {
        NavigationStack {
            ZStack {
                MapBackdrop(timeOfDay: .evening)

                List {
                    ForEach(Self.presets, id: \.id) { preset in
                        Button {
                            selectedSoundId = preset.id
                            AudioServicesPlaySystemSound(preset.soundID)
                        } label: {
                            HStack {
                                Text(preset.label)
                                    .foregroundStyle(.white)
                                Spacer()
                                if selectedSoundId == preset.id {
                                    Image(systemName: "checkmark")
                                        .foregroundStyle(PassColors.brand)
                                        .fontWeight(.bold)
                                }
                            }
                            .contentShape(Rectangle())
                        }
                        .listRowBackground(Color.white.opacity(0.08))
                    }
                }
                .scrollContentBackground(.hidden)
            }
            .navigationTitle("アラーム音")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("完了") { dismiss() }
                        .foregroundStyle(.white)
                }
            }
            .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
            .toolbarColorScheme(.dark, for: .navigationBar)
        }
    }
}
