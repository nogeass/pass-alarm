import Foundation

protocol HolidayRepositoryProtocol: Sendable {
    func fetchAll() async throws -> [HolidayJP]
    func fetchByDateRange(from: String, to: String) async throws -> [HolidayJP]
    func isHoliday(_ date: String) async throws -> Bool
    func insertAll(_ holidays: [HolidayJP]) async throws
}
