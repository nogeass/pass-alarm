// swift-tools-version: 6.0

import PackageDescription

let package = Package(
    name: "PassAlarm",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "PassAlarm", targets: ["PassAlarm"])
    ],
    dependencies: [
        .package(url: "https://github.com/groue/GRDB.swift.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "PassAlarm",
            dependencies: [
                .product(name: "GRDB", package: "GRDB.swift")
            ],
            path: "PassAlarm"
        )
    ]
)
