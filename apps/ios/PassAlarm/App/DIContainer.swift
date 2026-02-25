import Foundation

@Observable
final class DIContainer {
    static let shared = DIContainer()

    // Database
    let database: AppDatabase

    // Repositories
    let alarmPlanRepository: AlarmPlanRepositoryProtocol
    let skipExceptionRepository: SkipExceptionRepositoryProtocol
    let scheduledTokenRepository: ScheduledTokenRepositoryProtocol
    let holidayRepository: HolidayRepositoryProtocol
    let subscriptionRepository: SubscriptionRepositoryProtocol
    let appSettingsRepository: AppSettingsRepositoryProtocol
    let serverEntitlementRepository: ServerEntitlementRepositoryProtocol

    // Services
    let notificationScheduler: NotificationSchedulerProtocol
    let notificationPermission: NotificationPermissionProtocol
    let authService: AuthServiceProtocol

    // UseCases
    let computeQueueUseCase: ComputeQueueUseCase
    let rescheduleNextNUseCase: RescheduleNextNUseCase
    let skipDateUseCase: SkipDateUseCase
    let enablePlanUseCase: EnablePlanUseCase
    let updatePlanUseCase: UpdatePlanUseCase
    let onAlarmFiredUseCase: OnAlarmFiredUseCase
    let checkProLimitUseCase: CheckProLimitUseCase
    let createPlanUseCase: CreatePlanUseCase
    let deletePlanUseCase: DeletePlanUseCase
    let seedDefaultAlarmsUseCase: SeedDefaultAlarmsUseCase
    let updateAppSettingsUseCase: UpdateAppSettingsUseCase

    private init() {
        // Database
        database = AppDatabase.shared

        // Services
        let firebaseAuth = FirebaseAuthService()
        authService = firebaseAuth
        notificationScheduler = UNNotificationSchedulerAdapter()
        notificationPermission = UNNotificationPermissionAdapter()

        // Repositories
        alarmPlanRepository = GRDBAlarmPlanRepository(database: database)
        skipExceptionRepository = GRDBSkipExceptionRepository(database: database)
        scheduledTokenRepository = GRDBScheduledTokenRepository(database: database)
        holidayRepository = GRDBHolidayRepository(database: database)
        appSettingsRepository = UserDefaultsAppSettingsRepository()
        let serverEntitlements = ServerEntitlementRepository(authService: firebaseAuth)
        serverEntitlementRepository = serverEntitlements
        let storeKitRepo = StoreKitSubscriptionRepository()
        subscriptionRepository = CompositeSubscriptionRepository(
            storeKitRepository: storeKitRepo,
            serverEntitlementRepository: serverEntitlements
        )

        // UseCases
        computeQueueUseCase = ComputeQueueUseCase(
            planRepository: alarmPlanRepository,
            skipRepository: skipExceptionRepository,
            holidayRepository: holidayRepository,
            appSettingsRepository: appSettingsRepository
        )
        rescheduleNextNUseCase = RescheduleNextNUseCase(
            computeQueue: computeQueueUseCase,
            tokenRepository: scheduledTokenRepository,
            scheduler: notificationScheduler
        )
        skipDateUseCase = SkipDateUseCase(
            skipRepository: skipExceptionRepository,
            reschedule: rescheduleNextNUseCase
        )
        enablePlanUseCase = EnablePlanUseCase(
            planRepository: alarmPlanRepository,
            reschedule: rescheduleNextNUseCase
        )
        updatePlanUseCase = UpdatePlanUseCase(
            planRepository: alarmPlanRepository,
            reschedule: rescheduleNextNUseCase
        )
        onAlarmFiredUseCase = OnAlarmFiredUseCase(
            tokenRepository: scheduledTokenRepository,
            reschedule: rescheduleNextNUseCase
        )
        checkProLimitUseCase = CheckProLimitUseCase(
            planRepository: alarmPlanRepository,
            subscriptionRepository: subscriptionRepository
        )
        createPlanUseCase = CreatePlanUseCase(
            planRepository: alarmPlanRepository,
            checkProLimit: checkProLimitUseCase,
            reschedule: rescheduleNextNUseCase
        )
        deletePlanUseCase = DeletePlanUseCase(
            planRepository: alarmPlanRepository,
            reschedule: rescheduleNextNUseCase
        )
        seedDefaultAlarmsUseCase = SeedDefaultAlarmsUseCase(
            planRepository: alarmPlanRepository,
            reschedule: rescheduleNextNUseCase
        )
        updateAppSettingsUseCase = UpdateAppSettingsUseCase(
            appSettingsRepository: appSettingsRepository,
            reschedule: rescheduleNextNUseCase
        )
    }

    /// For testing
    init(database: AppDatabase,
         alarmPlanRepository: AlarmPlanRepositoryProtocol,
         skipExceptionRepository: SkipExceptionRepositoryProtocol,
         scheduledTokenRepository: ScheduledTokenRepositoryProtocol,
         holidayRepository: HolidayRepositoryProtocol,
         subscriptionRepository: SubscriptionRepositoryProtocol,
         appSettingsRepository: AppSettingsRepositoryProtocol,
         notificationScheduler: NotificationSchedulerProtocol,
         notificationPermission: NotificationPermissionProtocol,
         authService: AuthServiceProtocol,
         serverEntitlementRepository: ServerEntitlementRepositoryProtocol) {
        self.database = database
        self.alarmPlanRepository = alarmPlanRepository
        self.skipExceptionRepository = skipExceptionRepository
        self.scheduledTokenRepository = scheduledTokenRepository
        self.holidayRepository = holidayRepository
        self.subscriptionRepository = subscriptionRepository
        self.appSettingsRepository = appSettingsRepository
        self.notificationScheduler = notificationScheduler
        self.notificationPermission = notificationPermission
        self.authService = authService
        self.serverEntitlementRepository = serverEntitlementRepository
        self.computeQueueUseCase = ComputeQueueUseCase(
            planRepository: alarmPlanRepository,
            skipRepository: skipExceptionRepository,
            holidayRepository: holidayRepository,
            appSettingsRepository: appSettingsRepository
        )
        self.rescheduleNextNUseCase = RescheduleNextNUseCase(
            computeQueue: computeQueueUseCase,
            tokenRepository: scheduledTokenRepository,
            scheduler: notificationScheduler
        )
        self.skipDateUseCase = SkipDateUseCase(
            skipRepository: skipExceptionRepository,
            reschedule: rescheduleNextNUseCase
        )
        self.enablePlanUseCase = EnablePlanUseCase(
            planRepository: alarmPlanRepository,
            reschedule: rescheduleNextNUseCase
        )
        self.updatePlanUseCase = UpdatePlanUseCase(
            planRepository: alarmPlanRepository,
            reschedule: rescheduleNextNUseCase
        )
        self.onAlarmFiredUseCase = OnAlarmFiredUseCase(
            tokenRepository: scheduledTokenRepository,
            reschedule: rescheduleNextNUseCase
        )
        self.checkProLimitUseCase = CheckProLimitUseCase(
            planRepository: alarmPlanRepository,
            subscriptionRepository: subscriptionRepository
        )
        self.createPlanUseCase = CreatePlanUseCase(
            planRepository: alarmPlanRepository,
            checkProLimit: checkProLimitUseCase,
            reschedule: rescheduleNextNUseCase
        )
        self.deletePlanUseCase = DeletePlanUseCase(
            planRepository: alarmPlanRepository,
            reschedule: rescheduleNextNUseCase
        )
        self.seedDefaultAlarmsUseCase = SeedDefaultAlarmsUseCase(
            planRepository: alarmPlanRepository,
            reschedule: rescheduleNextNUseCase
        )
        self.updateAppSettingsUseCase = UpdateAppSettingsUseCase(
            appSettingsRepository: appSettingsRepository,
            reschedule: rescheduleNextNUseCase
        )
    }
}
