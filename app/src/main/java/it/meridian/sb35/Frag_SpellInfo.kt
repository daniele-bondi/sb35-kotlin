package it.meridian.sb35

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import it.meridian.sb35.database.SpellInfoLevel
import it.meridian.sb35.database.SpellInfoViewModel
import it.meridian.sb35.utils.*
import androidx.navigation.fragment.NavHostFragment
import it.meridian.sb35.database.SpellInfo
import java.net.URLDecoder




class Frag_SpellInfo: android.support.v4.app.Fragment()
{
	private lateinit var viewModel: SpellInfoViewModel
	private var layout : View? = null
	
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setHasOptionsMenu(true) // NOTE: Hack to hide the options menu from previous fragment
		
		this.viewModel = ViewModelProviders.of(this).get()
		
		val spell = this.arguments!!.getString(ARG_SPELL)
		this.doAsyncButWait {
			this.viewModel.loadData(spell)
		}.andThen {
			this.view!!.visibility = View.VISIBLE
			this@Frag_SpellInfo.populateView(this.viewModel.data, this.viewModel.levels)
		}.runNonExclusive()
	}
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		if(this.layout == null) {
			this.layout = inflater.inflate(R.layout.frag_nav_spell, container, false).also { layout ->
				layout.visibility = if(this.viewModel.data == null) View.INVISIBLE else View.VISIBLE
				layout.getChildAt<LinearLayout>(0).getChildAt<WebView>(2).apply {
					setBackgroundColor(0)
					settings.textZoom = 80
					webViewClient = WebViewClient()
				}
			}
		}
		return this.layout
	}
	
	
	override fun onStart()
	{
		super.onStart()
		this.myActivity.supportActionBar!!.title = this.arguments!!.getString(ARG_SPELL)
	}
	
	
	private fun populateView(info: SpellInfo?, levels: List<SpellInfoLevel>?)
	{
		if(info == null)
			return
		if(levels == null)
			return
		
		val table = this.view!!.getChildAt<View>(0).getChildAt<TableLayout>(0)
		
		table.getChildAt(0x0).getChildAt<TextView>(1).apply{
			text = info.book + " ${info.page}".takeIf{info.page != null}
		}
		table.getChildAt(0x1).getChildAt<TextView>(1).apply{
			text = concatenateLevels(levels)
		}
		table.getChildAt(0x2).getChildAt<TextView>(1).apply{
			text = if(info.subschool != null) "${info.school} (${info.subschool})" else info.school
		}
		table.getChildAt(0x3).apply{visibility = View.VISIBLE.takeIf{info.descriptors != null} ?: View.GONE}.getChildAt<TextView>(1).apply{
			text = info.descriptors
		}
		table.getChildAt(0x4).getChildAt<TextView>(1).apply{
			text = info.components
		}
		table.getChildAt(0x5).getChildAt<TextView>(1).apply{
			text = info.cast_time
		}
		table.getChildAt(0x6).apply{visibility = View.VISIBLE.takeIf{info.range != null} ?: View.GONE}.getChildAt<TextView>(1).apply{
			text = info.range
		}
		table.getChildAt(0x7).apply{visibility = View.VISIBLE.takeIf{info.effect_type != null && info.effect != null} ?: View.GONE}.apply{
			getChildAt<TextView>(0).text = info.effect_type
			getChildAt<TextView>(1).text = info.effect
		}
		table.getChildAt(0x8).getChildAt<TextView>(1).apply{
			text = info.duration
		}
		table.getChildAt(0x9).apply{visibility = View.VISIBLE.takeIf{info.saving_throw != null} ?: View.GONE}.getChildAt<TextView>(1).apply{
			text = info.saving_throw
		}
		table.getChildAt(0xA).apply{visibility = View.VISIBLE.takeIf{info.resistance != null} ?: View.GONE}.getChildAt<TextView>(1).apply{
			text = info.resistance
		}
		
		this.view!!.getChildAt<LinearLayout>(0).getChildAt<TextView>(1).apply {
			visibility = View.VISIBLE.takeIf{info.fluff != null} ?: View.GONE
			text = info.fluff
		}
		
		this.view!!.getChildAt<LinearLayout>(0).getChildAt<WebView>(2).apply {
			loadDataWithBaseURL("sb35://0.0.0.0/", info.description, null, "UTF-8", null)
		}
	}
	
	
	private fun concatenateLevels(levels: List<SpellInfoLevel>) : StringBuilder
	{
		return StringBuilder().apply {
			levels.forEachIndexed { i, info ->
				append(info.source).append(" ").append(info.level)
				if(i < levels.size - 1)
					append(", ")
			}
		}
	}
	
	
	private inner class WebViewClient : android.webkit.WebViewClient()
	{
		@Suppress("OverridingDeprecatedMember")
		override fun shouldOverrideUrlLoading(view: WebView, url: String)                 : Boolean = this.loadPage(URLDecoder.decode(url, "UTF-8").removePrefix("sb35://0.0.0.0/"))
		override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) : Boolean = this.loadPage((request.url.path.substring(1)))
		
		private fun loadPage(spell: String) : Boolean
		{
			val args = Bundle().apply{ putString(ARG_SPELL, spell) }
			NavHostFragment.findNavController(this@Frag_SpellInfo).navigate(R.id.frag_nav_spell, args)
			return true
		}
	}
}
