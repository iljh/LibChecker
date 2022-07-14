package com.absinthe.libchecker.bean

import com.chad.library.adapter.base.entity.node.BaseNode

abstract class SnapshotAbsItem : BaseNode() {
  override val childNode: MutableList<BaseNode>? = null
}
