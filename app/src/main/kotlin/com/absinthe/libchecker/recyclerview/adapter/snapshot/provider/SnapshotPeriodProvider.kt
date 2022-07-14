package com.absinthe.libchecker.recyclerview.adapter.snapshot.provider

import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LifecycleCoroutineScope
import com.absinthe.libchecker.R
import com.absinthe.libchecker.bean.SnapshotPeriodItem
import com.absinthe.libchecker.utils.extensions.getDimensionPixelSize
import com.absinthe.libchecker.utils.extensions.getStringArray
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder

const val SNAPSHOT_PERIOD_PROVIDER = 0

class SnapshotPeriodProvider(val lifecycleScope: LifecycleCoroutineScope) : BaseNodeProvider() {

  override val itemViewType: Int = SNAPSHOT_PERIOD_PROVIDER
  override val layoutId: Int = 0

  private val periodMap: Map<SnapshotPeriodItem.Type, String> by lazy {
    val array = context.getStringArray(R.array.list_snapshot_period)
    mapOf(
      SnapshotPeriodItem.Type.InFiveMinutes to array[0],
      SnapshotPeriodItem.Type.InOneHour to array[1],
      SnapshotPeriodItem.Type.InEightHours to array[2],
      SnapshotPeriodItem.Type.InOneDay to array[3],
      SnapshotPeriodItem.Type.InOneWeek to array[4],
      SnapshotPeriodItem.Type.Earlier to array[5],
      SnapshotPeriodItem.Type.PreInstalled to array[6]
    )
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    return BaseViewHolder(
      AppCompatTextView(context).also {
        it.layoutParams = ViewGroup.MarginLayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val padding = context.getDimensionPixelSize(R.dimen.main_card_padding)
        it.setPadding(padding, padding, padding, padding)
      }
    )
  }

  override fun convert(helper: BaseViewHolder, item: BaseNode) {
    (helper.itemView as AppCompatTextView).text = periodMap[(item as SnapshotPeriodItem).period]
  }
}
