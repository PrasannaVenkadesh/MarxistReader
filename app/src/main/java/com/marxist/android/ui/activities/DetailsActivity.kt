package com.marxist.android.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.marxist.android.R
import com.marxist.android.database.entities.LocalFeeds
import com.marxist.android.model.ShowSnackBar
import com.marxist.android.ui.base.BaseActivity
import com.marxist.android.utils.AppPreference.get
import com.marxist.android.utils.DeviceUtils
import com.marxist.android.utils.PrintLog
import com.marxist.android.utils.RxBus
import kotlinx.android.synthetic.main.activity_details.*
import org.sufficientlysecure.htmltextview.HtmlTextView
import kotlin.math.roundToInt

class DetailsActivity : BaseActivity() {
    private var article: LocalFeeds? = null
    private var readPercent: Int = 0

    companion object {
        const val ARTICLE = "article"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.menu_share) {
            DeviceUtils.shareIntent(article!!.title, article!!.link, applicationContext)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        setSupportActionBar(toolbar)
        article = intent.getSerializableExtra(ARTICLE) as LocalFeeds

        article!!.title.apply {
            txtCollapseTitle.text = this
            txtFeedTitle.text = this
        }

        val fontSize = appPreference[getString(R.string.pref_key_font_size), 14]

        if (article!!.audioUrl.isEmpty()) {
            fabAudio.hide()
        }

        var content = article!!.content
        try {
            val regex1 = Regex("(?s)<div class=\"wpcnt\">.*?</div>")
            content = content.replace(regex1, "")
            val regex2 = Regex("(?s)<script type=\"text/javascript\">.*?</script>")
            content = content.replace(regex2, "")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            txtContent.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
                setHtml(content)
            }
        }

        article!!.isBookMarked.apply {
            ivBookMark.tag = this
            if (this) {
                ivBookMark.playAnimation()
            }
        }

        initListeners()
    }

    private fun initListeners() {
        ivBookMark.setOnClickListener {
            val tag = it.tag as Boolean
            if (tag) {
                ivBookMark.playAnimation().apply {
                    ivBookMark.setAnimation(R.raw.bookmark_anim)
                }
            } else {
                ivBookMark.playAnimation()
            }

            ivBookMark.tag = !tag
            article!!.isBookMarked = !tag
            appDatabase.localFeedsDao()
                .updateBookMarkStatus(!tag, article!!.title, article!!.pubDate)
        }

        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var scrollRange = -1

            override fun onOffsetChanged(appBar: AppBarLayout?, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBar!!.totalScrollRange
                }
                txtFeedTitle.visibility = if (scrollRange + verticalOffset == 0) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            }
        })

        nestedScroll.setOnScrollChangeListener { scrollView: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollView != null) {
                val totalHeight =
                    scrollView.getChildAt(0).measuredHeight - scrollView.measuredHeight
//                isEndReached = scrollY == totalHeight
                readPercent = (scrollY.toDouble() / totalHeight.toDouble() * 100).roundToInt()
            }
        }

        ivBack.setOnClickListener {
            onBackPressed()
        }

        txtContent.customSelectionActionModeCallback =
            MarkTextSelectionActionMode(
                menuInflater,
                txtContent,
                applicationContext,
                article!!.title,
                article!!.link
            )
    }

    override fun onBackPressed() {
        if (article != null) {
            appDatabase.localFeedsDao()
                .updateReadPercentage(readPercent, article!!.title, article!!.pubDate)
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }

    class MarkTextSelectionActionMode(
        private val menuInflater: MenuInflater,
        private val txtContent: HtmlTextView,
        private val baseContext: Context,
        private val title: String,
        private val link: String
    ) :
        ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            if (item != null) {
                when (item.itemId) {
                    R.id.menu_share_item -> {
                        return try {
                            val selectedText = getSelected()
                            if (selectedText.isNotEmpty()) {
                                DeviceUtils.shareIntent(
                                    title,
                                    selectedText.plus("\n படிக்க : $link"),
                                    baseContext
                                )
                            }
                            mode!!.finish()
                            true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            false
                        }
                    }
                    R.id.menu_copy_item -> {
                        return try {
                            val selectedText = getSelected()
                            if (selectedText.isNotEmpty()) {
                                val clipManager =
                                    baseContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(
                                    "marxist_reader",
                                    selectedText.plus("\n படிக்க : $link")
                                )
                                clipManager.primaryClip = clip
                            }
                            RxBus.publish(ShowSnackBar(baseContext.getString(R.string.text_copied)))
                            mode!!.finish()
                            true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            false
                        }
                    }
                }
            }
            return false
        }

        private fun getSelected(): String {
            if (txtContent.isFocused) {
                val textStartIndex = txtContent.selectionStart
                val textEndIndex = txtContent.selectionEnd

                val min = 0.coerceAtLeast(textStartIndex.coerceAtMost(textEndIndex))
                val max = 0.coerceAtLeast(textStartIndex.coerceAtLeast(textEndIndex))
                return txtContent.text.subSequence(min, max).toString().trim { it <= ' ' }
            }
            return ""
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.menu_share_popup, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (menu != null) {
                menu.removeItem(android.R.id.cut)
                menu.removeItem(android.R.id.copy)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    menu.removeItem(android.R.id.shareText)
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            PrintLog.debug("Khaleel", "ActionMode Destroyed")
        }
    }


}