package me.urfate.yomuki.adapter.pages

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.urfate.yomuki.R
import me.urfate.yomuki.source.ContentSource.Companion.userAgent
import okhttp3.OkHttpClient

class PagesAdapter(
    var context: Context,
    var toolbar: Toolbar,
    private var chapterPager: TextView,
    private var actionBar: ActionBar,
    private var window: Window,
    private var sourceUrl: String?,
    private var images: MutableList<String>
) : RecyclerView.Adapter<PagesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_page, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.photoView.maximumScale = 8f
        holder.progressBar.setVisibilityAfterHide(View.GONE)

        val progressListener: ProgressListener = object : ProgressListener {
            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                val progress = (100 * bytesRead / contentLength).toInt()

                CoroutineScope(Dispatchers.Main).launch {
                    holder.progressBar.setProgressCompat(progress, true)
                }
            }
        }

        val imageLoader = ImageLoader.Builder(context).apply {
            okHttpClient(
                OkHttpClient.Builder()
                    .addInterceptor {
                        val originalResponse = it.proceed(it.request())
                        originalResponse.newBuilder()
                            .body(ProgressResponseBody(originalResponse.body!!, progressListener))
                            .build()
                    }::build
            )
            build()
        }

        holder.photoView.load(images[position], imageLoader.build()){
            crossfade(true)
            size(Size(960, 16000))
            addHeader("User-Agent", userAgent)
            addHeader("Accept", "text/html,application/xhtml+xml,application/xml;" +
                    "q=0.9,image/avif,image/jxl,image/webp,*/*;q=0.8")
            addHeader("Referer", sourceUrl ?: "https://google.com")
            listener(
                onStart = {
                    holder.progressBar.visibility = View.VISIBLE
                },
                onSuccess = { _: ImageRequest, _: SuccessResult ->
                    holder.progressBar.hide()
                },
                onError = { _: ImageRequest, result: ErrorResult ->
                    holder.progressBar.hide()
                    Toast.makeText(context, context.resources.getString(R.string.image_loading_error), Toast.LENGTH_SHORT).show()

                    result.throwable.printStackTrace()
                }
            )

        }

        holder.photoView.setOnScaleChangeListener { _: Float, _: Float, _: Float ->
            if (holder.photoView.scale <= 1.00f) {
                toolbar.animate()
                    .translationY(1f)
                    .alpha(1.0f)
                    .setDuration(160)
                    .setInterpolator(LinearInterpolator())
                    .withStartAction { actionBar.show() }
                chapterPager.animate()
                    .translationY(0f)
                    .alpha(1.0f)
                    .setDuration(160).interpolator = DecelerateInterpolator()
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            if (holder.photoView.scale > 1.0f) {
                toolbar.animate()
                    .translationY(0f)
                    .alpha(0f)
                    .setDuration(160)
                    .setInterpolator(LinearInterpolator())
                    .withEndAction { actionBar.hide() }
                chapterPager.animate()
                    .translationY(1f)
                    .alpha(0f)
                    .setDuration(160).interpolator = DecelerateInterpolator()
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
        holder.photoView.setOnClickListener {
            if (toolbar.translationY == 0f) {
                toolbar.animate()
                    .translationY(1f)
                    .alpha(1.0f)
                    .setDuration(160)
                    .setInterpolator(LinearInterpolator())
                    .withStartAction { actionBar.show() }
                chapterPager.animate()
                    .translationY(0f)
                    .alpha(1.0f)
                    .setDuration(160)
                    .setInterpolator(DecelerateInterpolator())
            } else {
                toolbar.animate()
                    .translationY(0f)
                    .alpha(0f)
                    .setDuration(160)
                    .setInterpolator(LinearInterpolator())
                    .withEndAction { actionBar.hide() }
                chapterPager.animate()
                    .translationY(1f)
                    .alpha(0f)
                    .setDuration(160)
                    .setInterpolator(DecelerateInterpolator())
            }
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val photoView: PhotoView
        val progressBar: CircularProgressIndicator

        init {
            photoView = view.findViewById(R.id.page_photo_view)
            progressBar = view.findViewById(R.id.page_progress_bar)
        }
    }
}