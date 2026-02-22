import Foundation

enum HolidaySeedLoader {
    static func loadBundledHolidays() -> [HolidayJP] {
        var all: [HolidayJP] = []
        for year in ["2025", "2026", "2027"] {
            if let url = Bundle.main.url(forResource: "holidays_jp_\(year)", withExtension: "json"),
               let data = try? Data(contentsOf: url) {
                let decoded = try? JSONDecoder().decode(HolidayFile.self, from: data)
                let holidays = decoded?.holidays.map { HolidayJP(date: $0.date, nameJa: $0.name) } ?? []
                all.append(contentsOf: holidays)
            }
        }
        return all
    }

    private struct HolidayFile: Codable {
        let year: Int
        let country: String
        let holidays: [Entry]

        struct Entry: Codable {
            let date: String
            let name: String
        }
    }
}
