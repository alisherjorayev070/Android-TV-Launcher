package uz.devlooper.androidtvlauncher.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView

open abstract class BaseVH(v: View) : RecyclerView.ViewHolder(v) {
    abstract fun bind()
}