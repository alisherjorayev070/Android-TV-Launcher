package uz.devlooper.androidtvlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import coil.load
import uz.devlooper.androidtvlauncher.databinding.ItemAppBinding

class AppsAdapter : ListAdapter<AppItemData, AppsAdapter.VH>(FlowerDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind()
    }

    private var onItemClickListener: ((AppItemData) -> Unit)? = null

    fun setOnIteClickListener(l: (AppItemData) -> Unit) {
        onItemClickListener = l
    }

    inner class VH(private val binding: ItemAppBinding) : BaseVH(binding.root) {

        init {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(getItem(absoluteAdapterPosition))
            }

            itemView.setOnFocusChangeListener { view, isFocused ->
                if (isFocused) {
                    itemView.scaleView(1.15f, 250)
                } else {
                    itemView.scaleView(1f, 250)
                }
            }
        }

        override fun bind() {
            val itemData = getItem(absoluteAdapterPosition) ?: return

            binding.image.load(itemData.createDrawable(itemView.context))
            binding.appName.text = itemData.displayName
        }
    }

    object FlowerDiffCallback : DiffUtil.ItemCallback<AppItemData>() {
        override fun areItemsTheSame(oldItem: AppItemData, newItem: AppItemData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppItemData, newItem: AppItemData): Boolean {
            return oldItem == newItem
        }
    }

}


fun View.scaleView(endScale: Float, time: Long) {
    animate()
        .scaleX(endScale)
        .scaleY(endScale)
        .setDuration(time)
        .setInterpolator(DecelerateInterpolator(1.5f))
        .withLayer()
        .start()
}


fun AppItemData.createDrawable(context: Context): Drawable {
    val packageManager = context.packageManager
    val intent = Intent.parseUri(launchIntentUriLeanback ?: launchIntentUriDefault, 0)

    return try {
        packageManager.getActivityBanner(intent) ?: packageManager.getActivityIcon(intent)
    } catch (err: PackageManager.NameNotFoundException) {
        packageManager.defaultActivityIcon
    }
}