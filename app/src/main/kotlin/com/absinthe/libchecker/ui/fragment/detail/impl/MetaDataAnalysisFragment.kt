package com.absinthe.libchecker.ui.fragment.detail.impl

import androidx.recyclerview.widget.DividerItemDecoration
import com.absinthe.libchecker.R
import com.absinthe.libchecker.annotation.METADATA
import com.absinthe.libchecker.databinding.FragmentLibNativeBinding
import com.absinthe.libchecker.recyclerview.diff.LibStringDiffUtil
import com.absinthe.libchecker.ui.detail.EXTRA_PACKAGE_NAME
import com.absinthe.libchecker.ui.fragment.BaseDetailFragment
import com.absinthe.libchecker.ui.fragment.EXTRA_TYPE
import com.absinthe.libchecker.ui.fragment.detail.LocatedCount
import com.absinthe.libchecker.utils.extensions.putArguments
import com.absinthe.libchecker.utils.showToast
import rikka.core.util.ClipboardUtils

class MetaDataAnalysisFragment : BaseDetailFragment<FragmentLibNativeBinding>() {

  override fun getRecyclerView() = binding.list
  override val needShowLibDetailDialog = false

  override fun init() {
    binding.apply {
      list.apply {
        adapter = this@MetaDataAnalysisFragment.adapter
      }
    }

    viewModel.metaDataItems.observe(viewLifecycleOwner) {
      if (it.isEmpty()) {
        emptyView.text.text = getString(R.string.empty_list)
      } else {
        context?.let { ctx ->
          binding.list.addItemDecoration(
            DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
          )
        }
        adapter.setDiffNewData(it.toMutableList(), navigateToComponentTask)
      }

      if (!isListReady) {
        viewModel.itemsCountLiveData.value = LocatedCount(locate = type, count = it.size)
        viewModel.itemsCountList[type] = it.size
        isListReady = true
      }
    }

    adapter.apply {
      animationEnable = true
      setOnItemLongClickListener { _, _, position ->
        ClipboardUtils.put(
          requireContext(),
          getItem(position).item.name + ": " + getItem(position).item.source
        )
        context.showToast(R.string.toast_copied_to_clipboard)
        true
      }
      setDiffCallback(LibStringDiffUtil())
      setEmptyView(emptyView)
    }
    viewModel.initMetaDataData(packageName)
  }

  companion object {
    fun newInstance(packageName: String): MetaDataAnalysisFragment {
      return MetaDataAnalysisFragment().putArguments(
        EXTRA_PACKAGE_NAME to packageName,
        EXTRA_TYPE to METADATA
      )
    }
  }
}
