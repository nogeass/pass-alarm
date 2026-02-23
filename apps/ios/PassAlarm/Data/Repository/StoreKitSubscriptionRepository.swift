import Foundation
import StoreKit

enum SubscriptionError: Error, Sendable {
    case productNotFound
    case verificationFailed
    case userCancelled
    case pending
    case unknown
}

final class StoreKitSubscriptionRepository: SubscriptionRepositoryProtocol, @unchecked Sendable {

    private static let monthlyID = "com.nogeass.passalarm.pro.monthly"
    private static let yearlyID  = "com.nogeass.passalarm.pro.yearly"
    private static let productIDs: Set<String> = [monthlyID, yearlyID]

    // MARK: - ObserveStatus

    func observeStatus() -> AsyncStream<ProStatus> {
        AsyncStream { continuation in
            let task = Task {
                // Emit current status first
                let initial = await currentStatus()
                continuation.yield(initial)

                // Then listen for transaction updates
                for await verificationResult in Transaction.updates {
                    guard let transaction = try? self.checkVerified(verificationResult) else {
                        continuation.yield(.free)
                        continue
                    }
                    await transaction.finish()
                    let status = self.mapTransactionToStatus(transaction)
                    continuation.yield(status)
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }

    // MARK: - CurrentStatus

    func currentStatus() async -> ProStatus {
        for await verificationResult in Transaction.currentEntitlements {
            guard let transaction = try? checkVerified(verificationResult) else { continue }
            if Self.productIDs.contains(transaction.productID),
               transaction.revocationDate == nil {
                return mapTransactionToStatus(transaction)
            }
        }
        return .free
    }

    // MARK: - FetchProducts

    func fetchProducts() async throws -> [ProProduct] {
        let storeProducts = try await Product.products(for: Self.productIDs)
        return storeProducts.compactMap { product in
            guard let period = Self.period(for: product.id) else { return nil }
            let pricePerMonth: String? = {
                guard period == .yearly else { return nil }
                let monthly = product.price / 12
                return product.priceFormatStyle.format(monthly)
            }()
            let trialText = Self.trialText(for: product)
            return ProProduct(
                id: product.id,
                period: period,
                displayPrice: product.displayPrice,
                pricePerMonth: pricePerMonth,
                trialText: trialText
            )
        }
    }

    // MARK: - Purchase

    func purchase(_ product: ProProduct) async throws -> ProStatus {
        let storeProducts = try await Product.products(for: [product.id])
        guard let storeProduct = storeProducts.first else {
            throw SubscriptionError.productNotFound
        }

        let result = try await storeProduct.purchase()

        switch result {
        case .success(let verificationResult):
            guard let transaction = try? checkVerified(verificationResult) else {
                throw SubscriptionError.verificationFailed
            }
            await transaction.finish()
            return mapTransactionToStatus(transaction)

        case .userCancelled:
            throw SubscriptionError.userCancelled

        case .pending:
            throw SubscriptionError.pending

        @unknown default:
            throw SubscriptionError.unknown
        }
    }

    // MARK: - RestorePurchases

    func restorePurchases() async throws -> ProStatus {
        try await AppStore.sync()
        return await currentStatus()
    }

    // MARK: - Private Helpers

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .verified(let value):
            return value
        case .unverified:
            throw SubscriptionError.verificationFailed
        }
    }

    private func mapTransactionToStatus(_ transaction: Transaction) -> ProStatus {
        let period = Self.period(for: transaction.productID) ?? .monthly
        return ProStatus(
            tier: .pro,
            expiresAt: transaction.expirationDate,
            period: period
        )
    }

    private static func period(for productID: String) -> ProPeriod? {
        switch productID {
        case monthlyID: return .monthly
        case yearlyID:  return .yearly
        default:        return nil
        }
    }

    private static func trialText(for product: Product) -> String? {
        guard let intro = product.subscription?.introductoryOffer,
              intro.paymentMode == .freeTrial else { return nil }
        let value = intro.period.value
        switch intro.period.unit {
        case .day:   return "\(value)日間無料"
        case .week:  return "\(value)週間無料"
        case .month: return "\(value)ヶ月無料"
        case .year:  return "\(value)年間無料"
        @unknown default: return nil
        }
    }
}
