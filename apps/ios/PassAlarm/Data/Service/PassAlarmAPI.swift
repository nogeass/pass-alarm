import Foundation

// MARK: - Response Types

struct ClaimResponse: Codable, Sendable {
    let ok: Bool?
    let error: String?
    let entitlement: ClaimEntitlement?

    struct ClaimEntitlement: Codable, Sendable {
        let tier: String
        let source: String
        let grantedAt: String

        enum CodingKeys: String, CodingKey {
            case tier
            case source
            case grantedAt = "granted_at"
        }
    }
}

struct EntitlementsResponse: Codable, Sendable {
    let entitlements: [EntitlementItem]

    struct EntitlementItem: Codable, Sendable {
        let id: Int
        let tier: String
        let source: String
        let grantedAt: String
        let expiresAt: String?

        enum CodingKeys: String, CodingKey {
            case id
            case tier
            case source
            case grantedAt = "granted_at"
            case expiresAt = "expires_at"
        }
    }
}

struct ConfigResponse: Codable, Sendable {
    let redeemDisabled: String?

    enum CodingKeys: String, CodingKey {
        case redeemDisabled = "REDEEM_DISABLED"
    }
}

// MARK: - API Errors

enum PassAlarmAPIError: LocalizedError, Sendable {
    case invalidURL
    case serverError(statusCode: Int, message: String?)
    case decodingError
    case authenticationRequired
    case tokenAlreadyUsed
    case tokenExpired
    case tokenRevoked
    case invalidToken
    case redeemDisabled

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "不正なURL"
        case .serverError(let code, let message):
            return message ?? "サーバーエラー (\(code))"
        case .decodingError:
            return "レスポンスの解析に失敗しました"
        case .authenticationRequired:
            return "認証が必要です"
        case .tokenAlreadyUsed:
            return "このトークンは既に使用されています"
        case .tokenExpired:
            return "トークンの有効期限が切れています"
        case .tokenRevoked:
            return "このトークンは無効化されています"
        case .invalidToken:
            return "無効なトークンです"
        case .redeemDisabled:
            return "特典の受け取りは一時的に停止中です"
        }
    }
}

// MARK: - API Client

enum PassAlarmAPI: Sendable {
    private static let baseURL = "https://pass-alarm-api.nogeass-inc.workers.dev"

    // MARK: - Claim Token

    static func claimToken(_ token: String, idToken: String) async throws -> ClaimResponse {
        guard let url = URL(string: "\(baseURL)/api/redeem/claim") else {
            throw PassAlarmAPIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(idToken)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15

        let body = ["token": token]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw PassAlarmAPIError.serverError(statusCode: -1, message: nil)
        }

        switch httpResponse.statusCode {
        case 200...299:
            do {
                return try JSONDecoder().decode(ClaimResponse.self, from: data)
            } catch {
                throw PassAlarmAPIError.decodingError
            }
        case 401:
            throw PassAlarmAPIError.authenticationRequired
        case 404:
            throw PassAlarmAPIError.invalidToken
        case 409:
            throw PassAlarmAPIError.tokenAlreadyUsed
        case 503:
            throw PassAlarmAPIError.redeemDisabled
        default:
            let errorMessage = parseErrorMessage(from: data)
            // Map known error messages
            if let msg = errorMessage {
                if msg.contains("expired") { throw PassAlarmAPIError.tokenExpired }
                if msg.contains("revoked") { throw PassAlarmAPIError.tokenRevoked }
            }
            throw PassAlarmAPIError.serverError(
                statusCode: httpResponse.statusCode,
                message: errorMessage
            )
        }
    }

    // MARK: - Get Entitlements

    static func getEntitlements(idToken: String) async throws -> EntitlementsResponse {
        guard let url = URL(string: "\(baseURL)/api/me/entitlements") else {
            throw PassAlarmAPIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("Bearer \(idToken)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw PassAlarmAPIError.serverError(statusCode: -1, message: nil)
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            if httpResponse.statusCode == 401 {
                throw PassAlarmAPIError.authenticationRequired
            }
            throw PassAlarmAPIError.serverError(
                statusCode: httpResponse.statusCode,
                message: parseErrorMessage(from: data)
            )
        }

        do {
            return try JSONDecoder().decode(EntitlementsResponse.self, from: data)
        } catch {
            throw PassAlarmAPIError.decodingError
        }
    }

    // MARK: - Get Config

    static func getConfig() async throws -> ConfigResponse {
        guard let url = URL(string: "\(baseURL)/api/config") else {
            throw PassAlarmAPIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.timeoutInterval = 15

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            let statusCode = (response as? HTTPURLResponse)?.statusCode ?? -1
            throw PassAlarmAPIError.serverError(statusCode: statusCode, message: nil)
        }

        do {
            return try JSONDecoder().decode(ConfigResponse.self, from: data)
        } catch {
            throw PassAlarmAPIError.decodingError
        }
    }

    // MARK: - Helpers

    private static func parseErrorMessage(from data: Data) -> String? {
        if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
           let error = json["error"] as? String {
            return error
        }
        return nil
    }
}
