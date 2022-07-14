package com.absinthe.libchecker.recyclerview.adapter.snapshot.provider

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import coil.load
import com.absinthe.libchecker.R
import com.absinthe.libchecker.bean.SnapshotDiffItem
import com.absinthe.libchecker.constant.Constants
import com.absinthe.libchecker.database.AppItemRepository
import com.absinthe.libchecker.utils.PackageUtils
import com.absinthe.libchecker.utils.extensions.getDrawable
import com.absinthe.libchecker.utils.extensions.setAlphaForAll
import com.absinthe.libchecker.utils.extensions.sizeToString
import com.absinthe.libchecker.view.detail.CenterAlignImageSpan
import com.absinthe.libchecker.view.snapshot.SnapshotItemView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder

const val ARROW = "→"
const val SNAPSHOT_ITEM_PROVIDER = 1

class SnapshotItemProvider(val lifecycleScope: LifecycleCoroutineScope) : BaseNodeProvider() {

  override val itemViewType: Int = SNAPSHOT_ITEM_PROVIDER
  override val layoutId: Int = 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    return BaseViewHolder(
      SnapshotItemView(context).also {
        it.layoutParams = ViewGroup.MarginLayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT
        )
      }
    )
  }

  override fun convert(helper: BaseViewHolder, item: BaseNode) {
    val snapshotItem = (item as? SnapshotDiffItem) ?: return
    (helper.itemView as SnapshotItemView).container.apply {
      val packageInfo = runCatching {
        AppItemRepository.allPackageInfoMap[snapshotItem.packageName]
          ?: PackageUtils.getPackageInfo(snapshotItem.packageName)
      }.getOrNull()

      if (packageInfo == null) {
        icon.load(R.drawable.ic_icon_blueprint)
      } else {
        icon.load(packageInfo)
      }

      if (snapshotItem.deleted) {
        setAlphaForAll(0.7f)
      } else {
        setAlphaForAll(1.0f)
      }

      val isNewOrDeleted = snapshotItem.deleted || snapshotItem.newInstalled

      stateIndicator.apply {
        added = snapshotItem.added && !isNewOrDeleted
        removed = snapshotItem.removed && !isNewOrDeleted
        changed = snapshotItem.changed && !isNewOrDeleted
        moved = snapshotItem.moved && !isNewOrDeleted
      }

      if (snapshotItem.isTrackItem) {
        val imageSpan = ImageSpan(context, R.drawable.ic_track)
        val spannable = SpannableString(" ${getDiffString(snapshotItem.labelDiff, isNewOrDeleted)}")
        spannable.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        appName.text = spannable
      } else {
        appName.text = getDiffString(snapshotItem.labelDiff, isNewOrDeleted)
      }

      if (isNewOrDeleted) {
        val labelDrawable = if (snapshotItem.newInstalled) {
          R.drawable.ic_label_new_package.getDrawable(context)!!
        } else {
          R.drawable.ic_label_deleted_package.getDrawable(context)!!
        }
        val sb = SpannableStringBuilder(appName.text)
        val spanString = SpannableString("   ")
        val span = CenterAlignImageSpan(
          labelDrawable.also {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
          }
        )
        spanString.setSpan(span, 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.append(spanString)
        appName.text = sb
      }

      packageName.text = snapshotItem.packageName
      versionInfo.text =
        getDiffString(
          snapshotItem.versionNameDiff,
          snapshotItem.versionCodeDiff,
          isNewOrDeleted,
          "%s (%s)"
        )

      if (snapshotItem.packageSizeDiff.old > 0L) {
        packageSizeInfo.isVisible = true
        val sizeDiff = SnapshotDiffItem.DiffNode(
          snapshotItem.packageSizeDiff.old.sizeToString(context),
          snapshotItem.packageSizeDiff.new?.sizeToString(context)
        )
        packageSizeInfo.text = getDiffString(sizeDiff, isNewOrDeleted)
      } else {
        packageSizeInfo.isGone = true
      }

      targetApiInfo.text = getDiffString(snapshotItem.targetApiDiff, isNewOrDeleted, "API %s")

      val oldAbiString = PackageUtils.getAbiString(context, snapshotItem.abiDiff.old.toInt(), false)
      val oldAbiSpanString: SpannableString
      var abiBadgeRes = PackageUtils.getAbiBadgeResource(snapshotItem.abiDiff.old.toInt())
      if (snapshotItem.abiDiff.old.toInt() != Constants.ERROR && snapshotItem.abiDiff.old.toInt() != Constants.OVERLAY && abiBadgeRes != 0) {
        var oldPaddingString = "  $oldAbiString"
        if (snapshotItem.abiDiff.old / Constants.MULTI_ARCH == 1) {
          oldPaddingString = "  $oldPaddingString"
        }
        oldAbiSpanString = SpannableString(oldPaddingString)
        abiBadgeRes.getDrawable(context)?.let {
          it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
          val span = CenterAlignImageSpan(it)
          oldAbiSpanString.setSpan(span, 0, 1, ImageSpan.ALIGN_BOTTOM)
        }
        if (snapshotItem.abiDiff.old / Constants.MULTI_ARCH == 1) {
          R.drawable.ic_multi_arch.getDrawable(context)?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            val span = CenterAlignImageSpan(it)
            oldAbiSpanString.setSpan(span, 2, 3, ImageSpan.ALIGN_BOTTOM)
          }
        }
      } else {
        oldAbiSpanString = SpannableString(oldAbiString)
      }
      val builder = SpannableStringBuilder(oldAbiSpanString)

      val newAbiSpanString: SpannableString
      if (snapshotItem.abiDiff.new != null) {
        val newAbiString =
          PackageUtils.getAbiString(context, snapshotItem.abiDiff.new.toInt(), false)
        abiBadgeRes = PackageUtils.getAbiBadgeResource(snapshotItem.abiDiff.new.toInt())
        if (snapshotItem.abiDiff.new.toInt() != Constants.ERROR && snapshotItem.abiDiff.new.toInt() != Constants.OVERLAY && abiBadgeRes != 0) {
          var newPaddingString = "  $newAbiString"
          if (snapshotItem.abiDiff.new / Constants.MULTI_ARCH == 1) {
            newPaddingString = "  $newPaddingString"
          }
          newAbiSpanString = SpannableString(newPaddingString)
          abiBadgeRes.getDrawable(context)?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            val span = CenterAlignImageSpan(it)
            newAbiSpanString.setSpan(span, 0, 1, ImageSpan.ALIGN_BOTTOM)
          }
          if (snapshotItem.abiDiff.new / Constants.MULTI_ARCH == 1) {
            R.drawable.ic_multi_arch.getDrawable(context)?.let {
              it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
              val span = CenterAlignImageSpan(it)
              newAbiSpanString.setSpan(span, 2, 3, ImageSpan.ALIGN_BOTTOM)
            }
          }
        } else {
          newAbiSpanString = SpannableString(newAbiString)
        }
      } else {
        newAbiSpanString = SpannableString("")
      }

      if (snapshotItem.abiDiff.new != null && snapshotItem.abiDiff.old != snapshotItem.abiDiff.new) {
        builder.append(" $ARROW ").append(newAbiSpanString)
      }
      abiInfo.text = builder
    }
  }

  private fun <T> getDiffString(
    diff: SnapshotDiffItem.DiffNode<T>,
    isNewOrDeleted: Boolean = false,
    format: String = "%s"
  ): String {
    return if (diff.old != diff.new && !isNewOrDeleted) {
      "${format.format(diff.old)} $ARROW ${format.format(diff.new)}"
    } else {
      format.format(diff.old)
    }
  }

  private fun getDiffString(
    diff1: SnapshotDiffItem.DiffNode<*>,
    diff2: SnapshotDiffItem.DiffNode<*>,
    isNewOrDeleted: Boolean = false,
    format: String = "%s"
  ): String {
    return if ((diff1.old != diff1.new || diff2.old != diff2.new) && !isNewOrDeleted) {
      "${format.format(diff1.old, diff2.old)} $ARROW ${format.format(diff1.new, diff2.new)}"
    } else {
      format.format(diff1.old, diff2.old)
    }
  }
}
