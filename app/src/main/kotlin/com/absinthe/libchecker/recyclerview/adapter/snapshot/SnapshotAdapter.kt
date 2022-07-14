package com.absinthe.libchecker.recyclerview.adapter.snapshot

import androidx.lifecycle.LifecycleCoroutineScope
import com.absinthe.libchecker.bean.SnapshotDiffItem
import com.absinthe.libchecker.bean.SnapshotPeriodItem
import com.absinthe.libchecker.recyclerview.adapter.snapshot.provider.SNAPSHOT_ITEM_PROVIDER
import com.absinthe.libchecker.recyclerview.adapter.snapshot.provider.SNAPSHOT_PERIOD_PROVIDER
import com.absinthe.libchecker.recyclerview.adapter.snapshot.provider.SnapshotItemProvider
import com.absinthe.libchecker.recyclerview.adapter.snapshot.provider.SnapshotPeriodProvider
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode

class SnapshotAdapter(val lifecycleScope: LifecycleCoroutineScope) : BaseNodeAdapter() {

  init {
    addNodeProvider(SnapshotItemProvider(lifecycleScope))
    addNodeProvider(SnapshotPeriodProvider(lifecycleScope))
  }

  override fun getItemType(data: List<BaseNode>, position: Int): Int {
    return when (data[position]) {
      is SnapshotDiffItem -> SNAPSHOT_ITEM_PROVIDER
      is SnapshotPeriodItem -> SNAPSHOT_PERIOD_PROVIDER
      else -> throw IllegalArgumentException("wrong snapshot provider item type")
    }
  }

  override fun getItemId(position: Int): Long {
    return runCatching {
      data[position].hashCode().toLong()
    }.getOrDefault(super.getItemId(position))
  }
}
