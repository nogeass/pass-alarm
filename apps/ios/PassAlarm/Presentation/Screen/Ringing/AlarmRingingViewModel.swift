import Foundation
import AudioToolbox
import AVFoundation

@Observable
final class AlarmRingingViewModel {
    var session: AlarmSession?
    var isPresented: Bool = false

    private var audioPlayer: AVAudioPlayer?
    private var timer: Timer?
    private var currentSoundId: String = "default"

    /// Maps soundId to a SystemSoundID for fallback playback.
    private static let soundMap: [String: SystemSoundID] = [
        "default": 1005,
        "alarm": 1304,
        "beacon": 1306,
        "bulletin": 1307,
        "radar": 1308,
        "signal": 1312,
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

            // Try bundled sound file first (alarm_default.caf)
            if currentSoundId == "default",
               let url = Bundle.main.url(forResource: "alarm_default", withExtension: "caf") {
                audioPlayer = try AVAudioPlayer(contentsOf: url)
                audioPlayer?.numberOfLoops = -1
                audioPlayer?.play()
                return
            }

            // For system sounds, use a repeating timer with AudioServicesPlaySystemSound
            if let systemSoundId = Self.soundMap[currentSoundId] {
                playSystemSoundLoop(systemSoundId)
                return
            }

            // Fallback to default bundled sound
            if let url = Bundle.main.url(forResource: "alarm_default", withExtension: "caf") {
                audioPlayer = try AVAudioPlayer(contentsOf: url)
                audioPlayer?.numberOfLoops = -1
                audioPlayer?.play()
            }
        } catch {
            print("Audio error: \(error)")
        }
    }

    private var systemSoundTimer: Timer?

    private func playSystemSoundLoop(_ soundId: SystemSoundID) {
        AudioServicesPlaySystemSound(soundId)
        systemSoundTimer = Timer.scheduledTimer(withTimeInterval: 2.0, repeats: true) { _ in
            AudioServicesPlaySystemSound(soundId)
        }
    }

    private func stopSound() {
        audioPlayer?.stop()
        audioPlayer = nil
        systemSoundTimer?.invalidate()
        systemSoundTimer = nil
    }
}
