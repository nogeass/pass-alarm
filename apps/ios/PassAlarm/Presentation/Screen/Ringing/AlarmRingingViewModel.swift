import Foundation
import AVFoundation

@Observable
@MainActor
final class AlarmRingingViewModel {
    var session: AlarmSession?
    var isPresented: Bool = false

    private var audioPlayer: AVAudioPlayer?
    private var timer: Timer?
    private var currentSoundId: String = "default"

    /// Maps soundId to the bundled .caf filename.
    private static let soundFileMap: [String: String] = [
        "default": "alarm_default",
        "alarm": "alarm_alarm",
        "beacon": "alarm_beacon",
        "bulletin": "alarm_bulletin",
        "radar": "alarm_radar",
        "signal": "alarm_signal",
    ]

    func startSession(plan: AlarmPlan) {
        currentSoundId = plan.soundId
        session = AlarmSession(
            planId: plan.id,
            totalRings: plan.repeatCount,
            intervalMin: plan.intervalMin,
            currentRingIndex: 1,
            isRinging: true,
            nextRingAt: nil
        )
        isPresented = true
        playSound()
    }

    /// Start a lightweight session from notification data (no full plan needed).
    func startFromNotification(soundId: String) {
        currentSoundId = soundId
        if session == nil {
            session = AlarmSession(
                planId: UUID(),
                totalRings: 1,
                intervalMin: 5,
                currentRingIndex: 1,
                isRinging: true,
                nextRingAt: nil
            )
        }
        isPresented = true
        playSound()
    }

    func stop() {
        stopSound()
        timer?.invalidate()
        session = nil
        isPresented = false
    }

    func snooze() {
        stopSound()
        guard var currentSession = session else { return }
        currentSession.currentRingIndex += 1
        currentSession.isRinging = false

        if currentSession.isComplete {
            stop()
            return
        }

        let nextRing = Date().addingTimeInterval(TimeInterval(currentSession.intervalMin * 60))
        currentSession.nextRingAt = nextRing
        session = currentSession

        timer = Timer.scheduledTimer(withTimeInterval: TimeInterval(currentSession.intervalMin * 60), repeats: false) { [weak self] _ in
            Task { @MainActor in
                self?.session?.isRinging = true
                self?.playSound()
            }
        }
    }

    private func playSound() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)

            let baseName = Self.soundFileMap[currentSoundId] ?? "alarm_default"
            if let url = Bundle.main.url(forResource: baseName, withExtension: "caf") {
                audioPlayer = try AVAudioPlayer(contentsOf: url)
                audioPlayer?.numberOfLoops = -1
                audioPlayer?.play()
            }
        } catch {
            print("Audio error: \(error)")
        }
    }

    private func stopSound() {
        audioPlayer?.stop()
        audioPlayer = nil
    }
}
