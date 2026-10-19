package com.flux.wear.di

import com.flux.core.data.alarm.AlarmTarget
import com.flux.core.data.alarm.AndroidAlarmScheduler
import com.flux.core.domain.port.AlarmScheduler
import com.flux.wear.alarm.WatchAlarmTarget
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WearModule {
    @Binds abstract fun alarmScheduler(impl: AndroidAlarmScheduler): AlarmScheduler

    @Binds abstract fun alarmTarget(impl: WatchAlarmTarget): AlarmTarget
}
