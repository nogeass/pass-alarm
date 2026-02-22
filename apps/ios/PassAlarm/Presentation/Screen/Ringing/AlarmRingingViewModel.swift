import Foundation
import AVFoundation

@Observable
final class AlarmRingingViewModel {
    var session: AlarmSession?
    var isPresented: Bool = false

    private var audioPlayer: AVAudioPlayer?
    private var timer: Timer?

    func startSession(plan: AlarmPlan) {
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

            if let url = Bundle.main.url(forResource: "alarm_default", withExtension: "caf") {
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
