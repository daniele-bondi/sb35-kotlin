package it.meridian.sb35

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.ExpandableListView
import android.widget.RadioButton
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import it.meridian.sb35.database.*
import it.meridian.sb35.utils.*


class Frag_Prepare: android.support.v4.app.Fragment()
{
	private lateinit var viewModel: CharacterKnownViewModel
	private lateinit var adapter: AdapterSingle
	private lateinit var selection: SpellSelectionResult
	private var showSummary = true
	
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setHasOptionsMenu(true)
		
		this.selection = SpellSelectionResult()
		this.viewModel = ViewModelProviders.of(this).get()
		this.adapter   = AdapterSingle()
		
		val character : String  = this.arguments!!.getString(ARG_CHARACTER)!!
		assert(this.arguments!!.containsKey(ARG_SLOT_ID))
		this.doAsyncButWait {
			this.viewModel.loadPreparable(character)
		}.andThen {
			this.viewModel.data!!.observe(this, this.observerChildren)
		}.runNonExclusive()
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		return inflater.inflateT<ExpandableListView>(R.layout.frag_page_known, container, false).also {
			it.setGroupIndicator(null)
			it.setAdapter(this.adapter)
			it.setOnChildClickListener { _, _, gPos, cPos, _ -> this.onListChildClick(gPos, cPos) }
		}
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.options_prepare, menu)
	override fun onOptionsItemSelected(item: MenuItem) : Boolean
	{
		when(item.itemId)
		{
			R.id.menu_action_done ->
			{
				if(this.selection.isNotEmpty()) {
					ViewModelProviders.of(this.activity!!).get<SelectionResultViewModel>().let {
						it.selection = this.selection
						it.arguments = Bundle(this.arguments)
					}
				}
				NavHostFragment.findNavController(this).navigate(R.id.nav_action_pop_to_character)
			}
		}
		return true
	}
	
	
	private fun onListChildClick(gPos: Int, cPos: Int): Boolean
	{
		val args = Bundle().also{ it.putString(ARG_SPELL, this.adapter.getChild(gPos, cPos).spell) }
		NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_spell, args)
		return false
	}
	
	
	private fun onChildSelectionSingle(known: Known, isChecked: Boolean)
	{
		if(isChecked) {
			this.selection.clear()
			this.selection.add(SpellSelection(known.spell, known.source, known.level))
		}
	}
	
	
	
	
	private val observerChildren = android.arch.lifecycle.Observer<List<Known>> { newChildren ->
		this.adapter.children = newChildren?.groupByKnown() ?: HashMap()
		this.adapter.groups = this.adapter.children.mapKnownGroup()
		this.adapter.dataSetObservable.notifyChanged()
	}
	
	private inner class AdapterSingle : android.widget.ExpandableListAdapter
	{
		var groups   : List<KnownGroup>       = ArrayList()
		var children : Map<Int, List<Known>>  = HashMap()
		
		
		override fun isEmpty()                                                 : Boolean = this.children.isEmpty()
		override fun hasStableIds()                                            : Boolean = true
		override fun isChildSelectable(groupPosition: Int, childPosition: Int) : Boolean = true
		override fun areAllItemsEnabled()                                      : Boolean = true
		
		override fun getGroupCount()                                           : Int        = this.groups.size
		override fun getGroup(groupPosition: Int)                              : KnownGroup = this.groups[groupPosition]
		override fun getGroupId(groupPosition: Int)                            : Long       = this.getGroup(groupPosition).id.toLong()
		override fun getCombinedGroupId(groupId: Long)                         : Long       = -groupId.lsb31
		
		override fun getChildrenCount(groupPosition: Int)                      : Int   = this.getGroup(groupPosition).count
		override fun getChild(groupPosition: Int, childPosition: Int)          : Known = this.children[this.groups[groupPosition].id]!![childPosition]
		override fun getChildId(groupPosition: Int, childPosition: Int)        : Long  = this.getChild(groupPosition, childPosition).id.toLong()
		override fun getCombinedChildId(groupId: Long, childId: Long)          : Long  = (groupId.lsb31 shl 32) or childId.lsb32
		
		val dataSetObservable = DataSetObservable()
		override fun registerDataSetObserver(observer: DataSetObserver?)   = dataSetObservable.registerObserver(observer)
		override fun unregisterDataSetObserver(observer: DataSetObserver?) = dataSetObservable.unregisterObserver(observer)
		override fun onGroupCollapsed(groupPosition: Int) {}
		override fun onGroupExpanded(groupPosition: Int)  {}
		
		@SuppressLint("SetTextI18n")
		override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val group = this.getGroup(groupPosition)
			
			val root = convertView ?: context.inflate(R.layout.view_expandable_list_group_1, parent, false)
			root.getChildAt<TextView>(0).text = "${group.source} ${group.level}"
			root.getChildAt<TextView>(1).text = group.count.toString()
			return root
		}
		
		@Suppress("DEPRECATION")
		@SuppressLint("SetTextI18n")
		override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val child = this.getChild(groupPosition, childPosition)
			val summaryVisibility = if(this@Frag_Prepare.showSummary) View.VISIBLE else View.GONE
			
			val root = convertView ?: context.inflate(R.layout.view_expandable_list_child_1, parent, false)
			root.getChildAt<TextView>(0).text = child.spell
			root.getChildAt<TextView>(1).apply{ text = Html.fromHtml(child.summary); visibility = summaryVisibility }
			root.getChildAt<RadioButton>(2).let {
				
				it.setOnCheckedChangeListener(null)
				it.isChecked = SpellSelection(child.spell, child.source, child.level) in selection
				
				it.setOnCheckedChangeListener{ _, isChecked ->
					this@Frag_Prepare.onChildSelectionSingle(child, isChecked)
					this@Frag_Prepare.view!!.cast<ExpandableListView>().invalidateViews()
				}
			}
			return root
		}
	}
}
