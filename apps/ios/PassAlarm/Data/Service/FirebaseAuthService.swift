import UIKit
import FirebaseAuth
import FirebaseCore
import AuthenticationServices
import CryptoKit
import GoogleSignIn

enum AuthError: LocalizedError, Sendable {
    case appleSignInFailed
    case googleSignInFailed
    case missingIDToken
    case firebaseAuthFailed(String)
    case notSignedIn

    var errorDescription: String? {
        switch self {
        case .appleSignInFailed:
            return "Apple Sign-Inに失敗しました"
        case .googleSignInFailed:
            return "Google Sign-Inに失敗しました"
        case .missingIDToken:
            return "IDトークンの取得に失敗しました"
        case .firebaseAuthFailed(let message):
            return "認証エラー: \(message)"
        case .notSignedIn:
            return "サインインしていません"
        }
    }
}

final class FirebaseAuthService: AuthServiceProtocol, @unchecked Sendable {

    private var currentNonce: String?

    nonisolated var currentUser: AuthUser? {
        guard let firebaseUser = Auth.auth().currentUser else { return nil }
        return AuthUser(
            uid: firebaseUser.uid,
            email: firebaseUser.email,
            displayName: firebaseUser.displayName
        )
    }

    @MainActor
    func signInWithApple() async throws -> AuthUser {
        let nonce = randomNonceString()
        currentNonce = nonce
        let hashedNonce = sha256(nonce)

        let credential = try await performAppleSignIn(hashedNonce: hashedNonce)

        guard let appleIDToken = credential.identityToken,
              let tokenString = String(data: appleIDToken, encoding: .utf8) else {
            throw AuthError.missingIDToken
        }

        let oauthCredential = OAuthProvider.appleCredential(
            withIDToken: tokenString,
            rawNonce: nonce,
            fullName: credential.fullName
        )

        let result = try await Auth.auth().signIn(with: oauthCredential)

        if let fullName = credential.fullName {
            let displayName = PersonNameComponentsFormatter.localizedString(
                from: fullName,
                style: .default
            )
            if !displayName.isEmpty, result.user.displayName == nil {
                let changeRequest = result.user.createProfileChangeRequest()
                changeRequest.displayName = displayName
                try? await changeRequest.commitChanges()
            }
        }

        return AuthUser(
            uid: result.user.uid,
            email: result.user.email,
            displayName: result.user.displayName
        )
    }

    @MainActor
    func signInWithGoogle() async throws -> AuthUser {
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            throw AuthError.googleSignInFailed
        }

        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene }).first,
              let rootVC = windowScene.windows.first?.rootViewController else {
            throw AuthError.googleSignInFailed
        }

        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config

        let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: rootVC)

        guard let idToken = result.user.idToken?.tokenString else {
            throw AuthError.missingIDToken
        }

        let credential = GoogleAuthProvider.credential(
            withIDToken: idToken,
            accessToken: result.user.accessToken.tokenString
        )

        let authResult = try await Auth.auth().signIn(with: credential)

        return AuthUser(
            uid: authResult.user.uid,
            email: authResult.user.email,
            displayName: authResult.user.displayName
        )
    }

    func signOut() throws {
        try Auth.auth().signOut()
    }

    func getIDToken() async throws -> String {
        guard let user = Auth.auth().currentUser else {
            throw AuthError.notSignedIn
        }
        return try await user.getIDToken()
    }

    nonisolated func observeAuthState() -> AsyncStream<AuthUser?> {
        AsyncStream { continuation in
            let handle = Auth.auth().addStateDidChangeListener { _, firebaseUser in
                if let firebaseUser {
                    continuation.yield(AuthUser(
                        uid: firebaseUser.uid,
                        email: firebaseUser.email,
                        displayName: firebaseUser.displayName
                    ))
                } else {
                    continuation.yield(nil)
                }
            }
            continuation.onTermination = { _ in
                Auth.auth().removeStateDidChangeListener(handle)
            }
        }
    }

    // MARK: - Apple Sign-In Helpers

    @MainActor
    private func performAppleSignIn(hashedNonce: String) async throws -> ASAuthorizationAppleIDCredential {
        try await withCheckedThrowingContinuation { continuation in
            let delegate = AppleSignInDelegate(continuation: continuation)
            objc_setAssociatedObject(
                delegate, "retainedDelegate", delegate, .OBJC_ASSOCIATION_RETAIN_NONATOMIC
            )

            let request = ASAuthorizationAppleIDProvider().createRequest()
            request.requestedScopes = [.fullName, .email]
            request.nonce = hashedNonce

            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = delegate
            controller.performRequests()
        }
    }

    private func randomNonceString(length: Int = 32) -> String {
        var randomBytes = [UInt8](repeating: 0, count: length)
        let result = SecRandomCopyBytes(kSecRandomDefault, randomBytes.count, &randomBytes)
        precondition(result == errSecSuccess, "Failed to generate random bytes")
        let charset: [Character] = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        return String(randomBytes.map { charset[Int($0) % charset.count] })
    }

    private func sha256(_ input: String) -> String {
        let data = Data(input.utf8)
        let hash = SHA256.hash(data: data)
        return hash.compactMap { String(format: "%02x", $0) }.joined()
    }
}

// MARK: - Apple Sign-In Delegate

private final class AppleSignInDelegate: NSObject, ASAuthorizationControllerDelegate, @unchecked Sendable {
    private let continuation: CheckedContinuation<ASAuthorizationAppleIDCredential, any Error>

    init(continuation: CheckedContinuation<ASAuthorizationAppleIDCredential, any Error>) {
        self.continuation = continuation
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        if let credential = authorization.credential as? ASAuthorizationAppleIDCredential {
            continuation.resume(returning: credential)
        } else {
            continuation.resume(throwing: AuthError.appleSignInFailed)
        }
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: any Error
    ) {
        continuation.resume(throwing: error)
    }
}
