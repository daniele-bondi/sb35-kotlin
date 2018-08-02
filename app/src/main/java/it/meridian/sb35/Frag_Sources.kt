package it.meridian.sb35

import android.arch.lifecycle.ViewModelProviders
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import it.meridian.sb35.database.*
import it.meridian.sb35.utils.*


class Frag_Sources : android.support.v4.app.Fragment()
{
	private lateinit var adapter: Adapter
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setHasOptionsMenu(true) // NOTE: Hack to hide the options menu from previous fragment
		
		this.adapter = Adapter(this.childFragmentManager)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View?
	{
		return inflater.inflateT<ViewPager>(R.layout.frag_nav_characters_character_sources, container, false).also {
			it.adapter = this.adapter
		}
	}
	
	override fun onStart()
	{
		super.onStart()
		
		val title = when(this.arguments!!.containsKey(ARG_CHARACTER)) {
			true -> R.string.select_source
			else -> R.string.spell_browser
		}
		this.myActivity.supportActionBar!!.title = this.getString(title)
	}
	
	
	
	
	private inner class Adapter(fragManager: FragmentManager) : FragmentPagerAdapter(fragManager)
	{
		override fun getCount() : Int = 3
		
		override fun getItem(position: Int) : android.support.v4.app.Fragment
		{
			return when(position)
			{
				0 -> Frag_Sources_Page().apply{arguments = Bundle(this@Frag_Sources.arguments).apply{putInt(ARG_SOURCE_TYPE, 1)}}
				1 -> Frag_Sources_Page().apply{arguments = Bundle(this@Frag_Sources.arguments).apply{putInt(ARG_SOURCE_TYPE, 2)}}
				2 -> Frag_Sources_Page().apply{arguments = Bundle(this@Frag_Sources.arguments).apply{putInt(ARG_SOURCE_TYPE, 3)}}
				else -> throw IllegalArgumentException()
			}
		}
		
		override fun getPageTitle(position: Int) : CharSequence?
		{
			return when(position)
			{
				0 -> this@Frag_Sources.getString(R.string.classes)
				1 -> this@Frag_Sources.getString(R.string.domains)
				2 -> this@Frag_Sources.getString(R.string.feats)
				else -> throw IllegalArgumentException()
			}
		}
	}
}




class Frag_Sources_Page : android.support.v4.app.Fragment()
{
	private lateinit var viewModel: SourceViewModel
	private lateinit var adapter: Adapter
	
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
//		this.setHasOptionsMenu(false)
		
		this.viewModel = ViewModelProviders.of(this).get()
		this.adapter = Adapter()
		
		val sourceType = this.arguments!!.getInt(ARG_SOURCE_TYPE)
		this.doAsyncButWait {
			this.viewModel.loadData(sourceType)
		}.andThen {
			this.adapter.items = this.viewModel.data ?: ArrayList()
			this.adapter.dataSetObservable.notifyChanged()
		}.runNonExclusive()
	}
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View?
	{
		return inflater.inflateT<ListView>(R.layout.frag_page_sources, container, false).also {
			it.isClickable = true
			it.adapter = this.adapter
			it.setOnItemClickListener { _, _, pos, _ -> onListItemClick(pos) }
		}
	}
	
	
	private fun onListItemClick(pos: Int)
	{
		val args = Bundle(this.arguments).also{
			it.putString(ARG_SOURCE, this.adapter.getItem(pos).name)
		}
		NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_source, args)
	}
	
	
	
	
	private inner class Adapter : android.widget.ListAdapter
	{
		var items : List<Source> = ArrayList()
		
		override fun isEnabled(position: Int)       : Boolean = true
		override fun areAllItemsEnabled()           : Boolean = true
		override fun hasStableIds()                 : Boolean = true
		override fun getItemViewType(position: Int) : Int     = 0
		override fun getViewTypeCount()             : Int     = 1
		override fun isEmpty()                      : Boolean = this.items.isEmpty()
		override fun getCount()                     : Int     = this.items.size
		override fun getItemId(position: Int)       : Long    = this.items[position].id.toLong()
		override fun getItem(position: Int)         : Source  = this.items[position]
		
		
		override fun getView(position: Int, convertView: View?, parent: ViewGroup?) : View
		{
			val view = (convertView ?: context.inflate(R.layout.view_sources_item, parent, false)) as TextView
			view.text = this.items[position].name
			return view
		}
		
		
		val dataSetObservable = DataSetObservable()
		override fun registerDataSetObserver(observer: DataSetObserver?)   = dataSetObservable.registerObserver(observer)
		override fun unregisterDataSetObserver(observer: DataSetObserver?) = dataSetObservable.unregisterObserver(observer)
	}
}
