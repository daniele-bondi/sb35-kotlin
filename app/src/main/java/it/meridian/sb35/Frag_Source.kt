package it.meridian.sb35

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.ExpandableListView
import android.widget.RadioButton
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import it.meridian.sb35.database.*
import it.meridian.sb35.utils.*


class Frag_Source: android.support.v4.app.Fragment()
{
	private lateinit var viewModel: SourceSpellViewModel
	private lateinit var adapter: AdapterBase
	private lateinit var selectionType: SelectionType
	private var          selection: SpellSelectionResult? = null
	
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setHasOptionsMenu(true)
		
		this.selectionType = this.arguments!!.getOptionalInt(ARG_MULTIPLICITY)?.toSelectionType() ?: SelectionType.NONE
		this.selection     = if(this.selectionType != SelectionType.NONE) SpellSelectionResult() else null
		this.viewModel     = ViewModelProviders.of(this).get()
		this.adapter       = when(this.selectionType) {
			SelectionType.NONE     -> AdapterNone()
			SelectionType.SINGLE   -> AdapterSingle(this.arguments!!.getString(ARG_SOURCE))
			SelectionType.MULTIPLE -> AdapterMultiple(this.arguments!!.getString(ARG_SOURCE))
		}
		
		val character : String? = this.arguments!!.getString(ARG_CHARACTER)
		val source    : String  = this.arguments!!.getString(ARG_SOURCE)
		this.doAsyncButWait {
			when(character) {
				null -> this.viewModel.loadData(source)
				else -> this.viewModel.loadDataUnknown(source, character)
			}
		}.andThen {
			this.adapter.children = this.viewModel.data?.groupBySourceSpell() ?: HashMap()
			this.adapter.groups = this.adapter.children.mapSourceSpellGroup()
			this.adapter.dataSetObservable.notifyChanged()
		}.runNonExclusive()
	}
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View?
	{
		return inflater.inflateT<ExpandableListView>(R.layout.frag_nav_characters_character_sources_source, container, false).also {
			it.setGroupIndicator(null)
			it.setAdapter(this.adapter)
			it.setOnChildClickListener { _, _, gPos, cPos, _ -> this.onListChildClick(gPos, cPos) }
		}
	}
	
	
	override fun onStart()
	{
		super.onStart()
		this.myActivity.supportActionBar!!.title = this.arguments?.getString(ARG_CHARACTER)
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.options_source, menu)
	override fun onPrepareOptionsMenu(menu: Menu) = menu.findItem(R.id.menu_action_done).let{it.isVisible = (this.selectionType != SelectionType.NONE)}
	override fun onOptionsItemSelected(item: MenuItem) : Boolean
	{
		when(item.itemId)
		{
			R.id.menu_action_done ->
			{
				if(this.selection!!.isNotEmpty())
				{
					ViewModelProviders.of(this.activity!!).get<SelectionResultViewModel>().let { viewModel ->
						viewModel.selection = this.selection!!
						viewModel.arguments = Bundle(this.arguments)
					}
				}
				
				NavHostFragment.findNavController(this).navigate(R.id.nav_action_return_to_character)
			}
		}
		return true
	}
	
	
	private fun onListChildClick(gPos: Int, cPos: Int): Boolean
	{
		val args = Bundle().also{
			it.putString(ARG_SPELL, this.adapter.getChild(gPos, cPos).name)
		}
		NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_spell, args)
		return false
	}
	
	private fun onChildSelectionSingle(spell: SourceSpell, isChecked: Boolean)
	{
		if(isChecked) {
			this.selection!!.clear()
			this.selection!!.add(SpellSelection(spell.name, spell.source, spell.level))
		}
	}
	
	private fun onChildSelectionMultiple(spell: SourceSpell, isSelected: Boolean)
	{
		if(isSelected)
			this.selection!!.add(SpellSelection(spell.name, spell.source, spell.level))
		else
			this.selection!!.remove(SpellSelection(spell.name, spell.source, spell.level))
	}
	
	private fun onGroupSelectionMultiple(group: List<SourceSpell>, isSelected: Boolean)
	{
		if(isSelected)
			this.selection!!.addAll(group.map{ SpellSelection(it.name, it.source, it.level) })
		else
			this.selection!!.removeAll(group.map{ SpellSelection(it.name, it.source, it.level) })
	}
	
	
	
	
	private abstract inner class AdapterBase : android.widget.ExpandableListAdapter
	{
		var groups: List<SourceSpellGroup> = ArrayList()
		var children: Map<Int, List<SourceSpell>> = HashMap()
		
		
		override fun isEmpty()                                                 : Boolean = this.children.isEmpty()
		override fun hasStableIds()                                            : Boolean = true
		override fun isChildSelectable(groupPosition: Int, childPosition: Int) : Boolean = true
		override fun areAllItemsEnabled()                                      : Boolean = true
		
		override fun getGroupCount()                                           : Int              = this.groups.size
		override fun getGroup(groupPosition: Int)                              : SourceSpellGroup = this.groups[groupPosition]
		override fun getGroupId(groupPosition: Int)                            : Long             = this.getGroup(groupPosition).level.toLong()
		override fun getCombinedGroupId(groupId: Long)                         : Long             = -groupId.lsb31
		
		override fun getChildrenCount(groupPosition: Int)                      : Int         = this.getGroup(groupPosition).count
		override fun getChild(groupPosition: Int, childPosition: Int)          : SourceSpell = this.children[this.groups[groupPosition].level]!![childPosition]
		override fun getChildId(groupPosition: Int, childPosition: Int)        : Long        = this.getChild(groupPosition, childPosition).id
		override fun getCombinedChildId(groupId: Long, childId: Long)          : Long        = (groupId.lsb31 shl 32) or childId.lsb32
		
		val dataSetObservable = DataSetObservable()
		override fun registerDataSetObserver(observer: DataSetObserver?)   = dataSetObservable.registerObserver(observer)
		override fun unregisterDataSetObserver(observer: DataSetObserver?) = dataSetObservable.unregisterObserver(observer)
		override fun onGroupCollapsed(groupPosition: Int) {}
		override fun onGroupExpanded(groupPosition: Int)  {}
	}
	
	
	
	
	private inner class AdapterNone : AdapterBase()
	{
		@SuppressLint("SetTextI18n")
		override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val group = this.getGroup(groupPosition)
			
			val root = convertView ?: context.inflate(R.layout.view_expandable_list_group_0, parent, false)
			root.getChildAt<TextView>(0).text = "${group.source} ${group.level}"
			root.getChildAt<TextView>(1).text = group.count.toString()
			return root
		}
		
		@SuppressLint("SetTextI18n")
		override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val spell = this.getChild(groupPosition, childPosition)
			
			val root = convertView ?: context.inflate(R.layout.view_expandable_list_child_0, parent, false)
			root.getChildAt<TextView>(0).text = spell.name
			root.getChildAt<TextView>(1).text = spell.summary
			return root
		}
	}
	
	
	
	
	private inner class AdapterSingle(private val source: String) : AdapterBase()
	{
		@SuppressLint("SetTextI18n")
		override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val group = this.getGroup(groupPosition)
			
			val root = convertView ?: context.inflate(R.layout.view_expandable_list_group_1, parent, false)
			root.getChildAt<TextView>(0).text = "${group.source} ${group.level}"
			root.getChildAt<TextView>(1).text = group.count.toString()
			return root
		}
		
		@SuppressLint("SetTextI18n")
		override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val spell = this.getChild(groupPosition, childPosition)
			
			val root = convertView ?: context.inflate(R.layout.view_expandable_list_child_1, parent, false)
			root.getChildAt<TextView>(0).text = spell.name
			root.getChildAt<TextView>(1).text = spell.summary
			root.getChildAt<RadioButton>(2).let {
				
				it.setOnCheckedChangeListener(null)
				it.isChecked = SpellSelection(spell.name, this.source, spell.level) in selection!!
				
				it.setOnCheckedChangeListener{ _, isChecked ->
					this@Frag_Source.onChildSelectionSingle(spell, isChecked)
					this@Frag_Source.view!!.cast<ExpandableListView>().invalidateViews()
				}
			}
			return root
		}
	}
	
	
	
	
	private inner class AdapterMultiple(private val source: String) : AdapterBase()
	{
		@SuppressLint("SetTextI18n")
		override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?) : View
		{
			val group = this.getGroup(groupPosition)
			val children = this.children[group.level] ?: ArrayList()
			val selectedChildrenCount = children.count{ SpellSelection(it.name, this.source, it.level) in selection!! }
			
			val root = convertView ?: context.inflate(R.layout.view_expandable_list_group_n, parent, false)
			root.getChildAt<TextView>(0).text = "${group.source} ${group.level}"
			root.getChildAt<TextView>(1).text = "$selectedChildrenCount / ${children.size}"
			root.getChildAt<CheckBox>(2).let {
				
				it.setOnCheckedChangeListener(null)
				it.isChecked = selectedChildrenCount == children.size
				
				it.setOnCheckedChangeListener { _, isChecked ->
					this@Frag_Source.onGroupSelectionMultiple(children, isChecked)
					this@Frag_Source.view!!.cast<ExpandableListView>().invalidateViews()
				}
			}
			return root
		}
		
		@SuppressLint("SetTextI18n")
		override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val spell = this.getChild(groupPosition, childPosition)
			
			val root = convertView ?: context.inflate(R.layout.view_expandable_list_child_n, parent, false)
			root.getChildAt<TextView>(0).text = spell.name
			root.getChildAt<TextView>(1).text = spell.summary
			root.getChildAt<CheckBox>(2).let {
				
				it.setOnCheckedChangeListener(null)
				it.isChecked = SpellSelection(spell.name, this.source, spell.level) in selection!!
				
				it.setOnCheckedChangeListener{ _, isChecked ->
					this@Frag_Source.onChildSelectionMultiple(spell, isChecked)
					this@Frag_Source.view!!.cast<ExpandableListView>().invalidateViews()
				}
			}
			return root
		}
	}
}
