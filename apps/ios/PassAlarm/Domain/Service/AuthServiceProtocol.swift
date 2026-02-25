import Foundation

struct AuthUser: Sendable, Equatable {
    let uid: String
    let email: String?
    let displayName: String?
}

protocol AuthServiceProtocol: Sendable {
    var currentUser: AuthUser? { get }
    func signInWithApple() async throws -> AuthUser
    func signInWithGoogle() async throws -> AuthUser
    func signOut() throws
    func getIDToken() async throws -> String
    func observeAuthState() -> AsyncStream<AuthUser?>
}
