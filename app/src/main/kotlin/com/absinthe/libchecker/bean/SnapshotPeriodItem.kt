package com.absinthe.libchecker.bean

data class SnapshotPeriodItem(val period: Type) : SnapshotAbsItem() {
  sealed class Type {
    object Zero : Type()
    object InFiveMinutes : Type()
    object InOneHour : Type()
    object InEightHours : Type()
    object InOneDay : Type()
    object InOneWeek : Type()
    object Earlier : Type()
    object PreInstalled : Type()
  }
}
