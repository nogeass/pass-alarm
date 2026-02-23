import Foundation

enum FeedbackAPI {
    private static let endpoint = URL(string: "https://pass-alarm-api.nogeass-inc.workers.dev/feedback")!
    private static let apiKey = "07b7d7c6ad899a7542bbe9a20ad61e5084f93cf6a5ca286144158a3ff0f9986b"

    static func send(
        message: String,
        appVersion: String,
        device: String,
        osVersion: String,
        platform: String
    ) async throws {
        var request = URLRequest(url: endpoint)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(apiKey, forHTTPHeaderField: "X-API-Key")
        request.timeoutInterval = 15

        let body: [String: String] = [
            "message": message,
            "appVersion": appVersion,
            "device": device,
            "osVersion": osVersion,
            "platform": platform,
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            let statusCode = (response as? HTTPURLResponse)?.statusCode ?? -1
            throw FeedbackError.serverError(statusCode: statusCode)
        }

        // Verify response has ok: true
        if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
           let ok = json["ok"] as? Bool, !ok {
            throw FeedbackError.rejected
        }
    }

    enum FeedbackError: LocalizedError {
        case serverError(statusCode: Int)
        case rejected

        var errorDescription: String? {
            switch self {
            case .serverError(let code): return "Server error (\(code))"
            case .rejected: return "Feedback was rejected"
            }
        }
    }
}
