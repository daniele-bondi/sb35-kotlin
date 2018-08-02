package it.meridian.sb35

import android.arch.lifecycle.ViewModelProviders
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.preference.PreferenceManager
import android.text.Html
import android.view.*
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import it.meridian.sb35.database.*
import it.meridian.sb35.utils.*
import java.util.Queue
import kotlin.math.max


class Frag_Character: android.support.v4.app.Fragment()
{
	private lateinit var adapter: Adapter
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.adapter = Adapter(this.childFragmentManager)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View?
	{
		return inflater.inflateT<ViewPager>(R.layout.frag_nav_characters_character, container, false).also {
			it.adapter = this.adapter
			it.currentItem = 1
		}
	}
	
	override fun onStart()
	{
		super.onStart()
		this.myActivity.supportActionBar!!.title = this.arguments?.getString(ARG_CHARACTER)
	}
	
	
	
	
	private inner class Adapter(fragManager: FragmentManager) : FragmentPagerAdapter(fragManager)
	{
		override fun getCount() : Int = 3
		
		override fun getItem(position: Int) : android.support.v4.app.Fragment?
		{
			return when(position)
			{
				0 -> Frag_Character_Scrolls().also { it.arguments = Bundle(this@Frag_Character.arguments) }
				1 -> Frag_Character_Slots().also   { it.arguments = Bundle(this@Frag_Character.arguments) }
				2 -> Frag_Character_Known().also   { it.arguments = Bundle(this@Frag_Character.arguments) }
				else -> throw IllegalArgumentException()
			}
		}
		
		override fun getPageTitle(position: Int) : CharSequence?
		{
			return when(position)
			{
				0 -> this@Frag_Character.getString(R.string.scrolls)
				1 -> this@Frag_Character.getString(R.string.slots)
				2 -> this@Frag_Character.getString(R.string.known)
				else -> throw IllegalArgumentException()
			}
		}
	}
}




class Frag_Character_Scrolls : android.support.v4.app.Fragment()
{
	private lateinit var viewModel: CharacterScrollViewModel
	private lateinit var adapter: Adapter
	private var showSummary = true
	
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setHasOptionsMenu(true)
		
		this.viewModel = ViewModelProviders.of(this).get()
		this.adapter   = Adapter()
		
		val character = this.arguments!!.getString(ARG_CHARACTER)
		this.doAsyncButWait {
			this.viewModel.loadData(character)
		}.andThen {
			this.viewModel.data!!.observe(this, this.observerChildren)
		}.runNonExclusive()
	}
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		return inflater.inflateT<ExpandableListView>(R.layout.frag_page_scrolls, container, false).also {
			it.setGroupIndicator(null)
			it.setAdapter(this.adapter)
			it.setOnChildClickListener{ _, _, gPos, cPos, _ -> this.onListChildClick(gPos, cPos) }
			it.setOnCreateContextMenuListener{menu, _, info -> this.onCreateChildContextMenu(menu, info as ExpandableListView.ExpandableListContextMenuInfo)}
		}
	}
	
	
	override fun onStart()
	{
		super.onStart()
		
		this.showSummary = PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("show_summary_scroll", true)
		
		val selectionResult = ViewModelProviders.of(this.activity!!).get<SelectionResultViewModel>()
		if(selectionResult.hasScrollSelection)
		{
			val character = this.arguments!!.getString(ARG_CHARACTER)!!
			val selection = selectionResult.selection!!
			val scrollType = selectionResult.scrollType
			selectionResult.clear()
			val ids = this.viewModel.findFreeIds(selection.size)
			
			this.doAsyncButWait {
				this.onSelectionResult(selection, character, scrollType, ids)
			}.runNonExclusive()
		}
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.options_character_scroll, menu)
	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		when(item.itemId)
		{
			R.id.menu_action_create -> makeRadioDialog(this.context!!, R.layout.dialog_scroll_type, R.string.choose_type, this::onScrollTypeSelected).show()
			
			R.id.menu_action_toggle_summary -> { this.showSummary = !this.showSummary ; this.view!!.cast<ExpandableListView>().invalidateViews() }
			R.id.menu_action_preferences    -> NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_preferences)
		}
		return true
	}
	
	
	private fun onCreateChildContextMenu(menu: ContextMenu, menuInfo: ExpandableListView.ExpandableListContextMenuInfo)
	{
		if(ExpandableListView.getPackedPositionType(menuInfo.packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_CHILD)
			return
		this.inflateMenu(R.menu.context_scroll, menu)
	}
	
	
	// NOTE: For some reason this function is called even when another fragment is visible
	override fun onContextItemSelected(item: MenuItem): Boolean
	{
		if(!this.userVisibleHint)
			return super.onContextItemSelected(item)
		
		val info = item.menuInfo as ExpandableListView.ExpandableListContextMenuInfo
		if(ExpandableListView.getPackedPositionType(info.packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_CHILD)
			return false
		
		val groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition)
		val childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition)
		val scroll = this.adapter.getChild(groupPosition, childPosition).toEntity()
		
		when(item.itemId)
		{
			R.id.menu_action_update ->
			{
				makeIntPickerDialog(this.context!!, scroll.caster_level, 20) { newLevel ->
					scroll.caster_level = newLevel
					this.doAsyncButWait{Database.instance?.daoCharacterScroll()?.update(scroll)}.runNonExclusive()
				}.show()
			}
			
			R.id.menu_action_delete ->
			{
				makeConfirmDialog(this.context!!) {
					this.doAsyncButWait{Database.instance?.daoCharacterScroll()?.delete(scroll)}.runNonExclusive()
				}.show()
			}
		}
		return true
	}
	
	
	private fun onListChildClick(groupPosition: Int, childPosition: Int) : Boolean
	{
		val args  = Bundle().also {
			it.putString(ARG_SPELL, this.adapter.getChild(groupPosition, childPosition).spell)
		}
		NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_spell, args)
		return true
	}
	
	
	private fun onScrollTypeSelected(selectedRadioId: Int)
	{
		ViewModelProviders.of(this.activity!!).get<SelectionResultViewModel>().also { viewModel ->
			viewModel.clear()
			viewModel.target = SelectionTarget.SCROLL
		}
		val args = Bundle().apply{ putInt(ARG_MULTIPLICITY, SelectionType.MULTIPLE.value) }
		when(selectedRadioId)
		{
			R.id.radio_button_arcane -> args.putInt(ARG_SCROLL_TYPE, Scroll.Type.Arcane.value)
			R.id.radio_button_divine -> args.putInt(ARG_SCROLL_TYPE, Scroll.Type.Divine.value)
		}
		NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_sources_scroll, args)
	}
	
	
	private fun onSelectionResult(selection: Collection<SpellSelection>, character: String, type: Scroll.Type, freeIds: Queue<Int>)
	{
		assert(selection.isNotEmpty())
		fun minCL(SL: Int) = max(1, SL*2-1)
		
		val toInsert = selection.map{ EntityScroll(character, freeIds.remove(), type.value, minCL(it.level), it.spell) }.toTypedArray()
		Database.instance!!.daoCharacterScroll().insert(*toInsert)
	}
	
	
	
	
	private val observerChildren = android.arch.lifecycle.Observer<List<Scroll>> { newChildren ->
		this.adapter.children = newChildren?.groupByScroll() ?: HashMap()
		this.adapter.groups = this.adapter.children.mapScrollGroup()
		this.adapter.dataSetObservable.notifyChanged()
	}
	
	// Frag_Character_Scrolls
	private inner class Adapter : android.widget.ExpandableListAdapter
	{
		var groups   : List<ScrollGroup> = ArrayList()
		var children : Map<Int, List<Scroll>>  = HashMap()
		
		
		override fun isEmpty()                                                 : Boolean = this.children.isEmpty()
		override fun hasStableIds()                                            : Boolean = true
		override fun isChildSelectable(groupPosition: Int, childPosition: Int) : Boolean = true
		override fun areAllItemsEnabled()                                      : Boolean = true
		
		override fun getGroupCount()                                           : Int         = this.groups.size
		override fun getGroup(groupPosition: Int)                              : ScrollGroup = this.groups[groupPosition]
		override fun getGroupId(groupPosition: Int)                            : Long        = this.getGroup(groupPosition).caster_level.toLong()
		override fun getCombinedGroupId(groupId: Long)                         : Long        = -groupId.lsb31
		
		override fun getChildrenCount(groupPosition: Int)                      : Int    = this.getGroup(groupPosition).count
		override fun getChild(groupPosition: Int, childPosition: Int)          : Scroll = this.children[this.groups[groupPosition].caster_level]!![childPosition]
		override fun getChildId(groupPosition: Int, childPosition: Int)        : Long   = this.getChild(groupPosition, childPosition).id.toLong()
		override fun getCombinedChildId(groupId: Long, childId: Long)          : Long   = (groupId.lsb31 shl 32) or childId.lsb32
		
		override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val group = this.getGroup(groupPosition)
			
			val root = convertView ?: context.inflate(R.layout.view_character_scroll_group, parent, false)
			root.getChildAt<TextView>(0).text = "Caster Level ${group.caster_level}"
			root.getChildAt<TextView>(1).text = group.count.toString()
			return root
		}
		
		@Suppress("DEPRECATION")
		override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?) : View
		{
			val child = this.getChild(groupPosition, childPosition)
			val summaryVisibility = if(this@Frag_Character_Scrolls.showSummary) View.VISIBLE else View.GONE
			
			val root = convertView ?: context.inflate(R.layout.view_character_scroll_child, parent, false)
			root.getChildAt<ImageView>(0).setImageDrawable(resources.getDrawable(child.type.iconRes, null))
			root.getChildAt<TextView>(1).text = child.spell
			root.getChildAt<TextView>(2).apply{ text = Html.fromHtml(child.summary); visibility = summaryVisibility }
			return root
		}
		
		val dataSetObservable = DataSetObservable()
		override fun registerDataSetObserver(observer: DataSetObserver?)   = dataSetObservable.registerObserver(observer)
		override fun unregisterDataSetObserver(observer: DataSetObserver?) = dataSetObservable.unregisterObserver(observer)
		override fun onGroupCollapsed(groupPosition: Int) {}
		override fun onGroupExpanded(groupPosition: Int)  {}
	}
}




class Frag_Character_Slots : android.support.v4.app.Fragment()
{
	private lateinit var viewModel: CharacterSlotViewModel
	private lateinit var adapter: Adapter
	private var showSummary = true
	
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setHasOptionsMenu(true)
		
		this.viewModel = ViewModelProviders.of(this).get()
		this.adapter   = Adapter()
		
		val character = this.arguments!!.getString(ARG_CHARACTER)
		this.doAsyncButWait {
			this.viewModel.loadData(character)
		}.andThen {
//			this.viewModel.groups!!.observe(this, this.observerGroups)
			this.viewModel.data!!.observe(this, this.observerChildren)
		}.runNonExclusive()
	}
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		return inflater.inflateT<ExpandableListView>(R.layout.frag_page_slots, container, false).also {
			it.setGroupIndicator(null)
			it.setAdapter(this.adapter)
			it.setOnChildClickListener{ _, _, gPos, cPos, _ -> this.onListChildClick(gPos, cPos) }
			it.setOnCreateContextMenuListener{menu, _, info -> this.onCreateChildContextMenu(menu, info as ExpandableListView.ExpandableListContextMenuInfo)}
		}
	}
	
	
	override fun onStart()
	{
		super.onStart()
		
		this.showSummary = PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("show_summary_slot", true)
		
		val selectionResult = ViewModelProviders.of(this.activity!!).get<SelectionResultViewModel>()
		if(selectionResult.hasSlotSelection)
		{
			val selection = selectionResult.selection!!
			val slotId    = selectionResult.slotId
			selectionResult.clear()
			
			this.doAsyncButWait {
				this.onSelectionResult(selection, slotId)
			}.runNonExclusive()
		}
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.options_character_slot, menu)
	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		when(item.itemId)
		{
			R.id.menu_action_create  -> { makeSlotCreationDialog(this.context!!, this::onSlotCreationConfirmed).show() }
			R.id.menu_action_recover -> { makeConfirmDialog(this.context!!, R.string.are_you_sure, this::onRecoverSlotsConfirmed).show() }
			
			R.id.menu_action_toggle_summary -> { this.showSummary = !this.showSummary ; this.view!!.cast<ExpandableListView>().invalidateViews() }
			R.id.menu_action_preferences    -> NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_preferences)
		}
		return true
	}
	
	
	private fun onCreateChildContextMenu(menu: ContextMenu, menuInfo: ExpandableListView.ExpandableListContextMenuInfo)
	{
		if(ExpandableListView.getPackedPositionType(menuInfo.packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_CHILD)
			return
		this.inflateMenu(R.menu.context_slot, menu)
		
		val groupPosition = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition)
		val childPosition = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition)
		val slot = this.adapter.getChild(groupPosition, childPosition)
		
		menu.findItem(R.id.menu_toggle_expended).isChecked = slot.expended
		menu.findItem(R.id.menu_action_clear).isEnabled    = slot.spell != null
		menu.findItem(R.id.menu_toggle_special).isChecked  = slot.special
	}
	
	
	// NOTE: For some reason this function is called even when another fragment is visible
	override fun onContextItemSelected(item: MenuItem): Boolean
	{
		if(!this.userVisibleHint)
			return super.onContextItemSelected(item)
		
		val info = item.menuInfo as ExpandableListView.ExpandableListContextMenuInfo
		if(ExpandableListView.getPackedPositionType(info.packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_CHILD)
			return false
		
		val groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition)
		val childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition)
		val slot = this.adapter.getChild(groupPosition, childPosition).toEntity()
		
		when(item.itemId)
		{
			R.id.menu_toggle_expended -> { slot.expended = !slot.expended ; this.doAsyncButWait{Database.instance?.daoCharacterSlot()?.update(slot)}.runNonExclusive() }
			R.id.menu_action_clear    -> { slot.spell    = null           ; this.doAsyncButWait{Database.instance?.daoCharacterSlot()?.update(slot)}.runNonExclusive() }
			R.id.menu_toggle_special  -> { slot.special  = !slot.special  ; this.doAsyncButWait{Database.instance?.daoCharacterSlot()?.update(slot)}.runNonExclusive() }
			R.id.menu_action_delete   -> { /*                            */ this.doAsyncButWait{Database.instance?.daoCharacterSlot()?.delete(slot)}.runNonExclusive() }
		}
		return true
	}
	
	
	private fun onListChildClick(groupPosition: Int, childPosition: Int) : Boolean
	{
		val slot = this.adapter.getChild(groupPosition, childPosition)
		if(slot.spell != null)
		{
			val args = Bundle().also {
				it.putString(ARG_SPELL, slot.spell)
			}
			NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_spell, args)
		}
		else
		{
			ViewModelProviders.of(this.activity!!).get<SelectionResultViewModel>().also { viewModel ->
				viewModel.clear()
				viewModel.target = SelectionTarget.SLOT
			}
			val args = Bundle().also {
				it.putString(ARG_CHARACTER, this.arguments!!.getString(ARG_CHARACTER))
				it.putInt(ARG_SLOT_ID, slot.id)
				it.putInt(ARG_MULTIPLICITY, SelectionType.SINGLE.value)
			}
			NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_slot_assign, args)
		}
		return true
	}
	
	
	private fun onSlotCreationConfirmed(result: Map<Int, Int>)
	{
		val totalSlotCount = result.values.sum()
		if(totalSlotCount == 0)
			return
		
		val character = this.arguments!!.getString(ARG_CHARACTER)
		val freeIds = this.viewModel.findFreeIds(totalSlotCount)
		
		this.doAsyncButWait {
			result.foreach { level, count ->
				(0 until count).forEach { _ ->
					val slotId = freeIds.remove()
					val slot = EntitySlot(character, slotId, level, null, false, false)
					Database.instance?.daoCharacterSlot()?.insert(slot)
				}
			}
		}.runNonExclusive()
	}
	
	
	private fun onRecoverSlotsConfirmed()
	{
		this.viewModel.data?.value?.let { slots ->
			this.doAsyncButWait {
				for(slot in slots)
					Database.instance?.daoCharacterSlot()?.update(slot.apply{expended = false}.toEntity())
			}.runNonExclusive()
		}
	}
	
	
	private fun onSelectionResult(selection: Collection<SpellSelection>, slotId: Int)
	{
		assert(selection.isNotEmpty())
		
		val item = selection.single()
		val slot = this.viewModel.data!!.value!!.single{it.id == slotId}.toEntity().apply{spell = item.spell}
		Database.instance!!.daoCharacterSlot().update(slot)
	}
	
	
	
	
	private val observerChildren = android.arch.lifecycle.Observer<List<Slot>> { newChildren ->
		this.adapter.children = newChildren?.groupBySlot() ?: HashMap()
		this.adapter.groups = this.adapter.children.mapSlotGroup()
		this.adapter.dataSetObservable.notifyChanged()
	}
	
	// Frag_Character_Slots
	private inner class Adapter : android.widget.ExpandableListAdapter
	{
		var groups   : List<SlotGroup> = ArrayList()
		var children : Map<Int, List<Slot>>  = HashMap()
		
		
		override fun isEmpty()                                                 : Boolean = this.children.isEmpty()
		override fun hasStableIds()                                            : Boolean = true
		override fun isChildSelectable(groupPosition: Int, childPosition: Int) : Boolean = true
		override fun areAllItemsEnabled()                                      : Boolean = true
		
		override fun getGroupCount()                                           : Int       = this.groups.size
		override fun getGroup(groupPosition: Int)                              : SlotGroup = this.groups[groupPosition]
		override fun getGroupId(groupPosition: Int)                            : Long      = this.getGroup(groupPosition).level.toLong()
		override fun getCombinedGroupId(groupId: Long)                         : Long      = -groupId.lsb31
		
		override fun getChildrenCount(groupPosition: Int)                      : Int  = this.getGroup(groupPosition).count
		override fun getChild(groupPosition: Int, childPosition: Int)          : Slot = this.children[this.groups[groupPosition].level]!![childPosition]
		override fun getChildId(groupPosition: Int, childPosition: Int)        : Long = this.getChild(groupPosition, childPosition).id.toLong()
		override fun getCombinedChildId(groupId: Long, childId: Long)          : Long = (groupId.lsb31 shl 32) or childId.lsb32
		
		override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val group = this.getGroup(groupPosition)
			
			val root = convertView ?: context.inflate(R.layout.view_character_slot_group, parent, false)
			root.getChildAt<TextView>(0).text = "Level ${group.level}"
			root.getChildAt<TextView>(1).text = "${group.avail} / ${group.count}"
			return root
		}
		
		@Suppress("DEPRECATION")
		override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val child = this.getChild(groupPosition, childPosition)
			val summaryVisibility = if(this@Frag_Character_Slots.showSummary) View.VISIBLE else View.GONE
			val backgroundColor = if(child.expended) getColor(R.color.overlayRed) else 0
			
			val root = convertView ?: context.inflate(R.layout.view_character_slot_child, parent, false)
			root.getChildAt<ImageView>(0).visibility = if(child.special) View.VISIBLE else View.GONE
			root.getChildAt<TextView>(1).text = child.spell
			root.getChildAt<TextView>(2).apply{ text = Html.fromHtml(child.summary ?: ""); visibility = summaryVisibility }
			root.setBackgroundColor(backgroundColor) // ARGB
			return root
		}
		
		val dataSetObservable = DataSetObservable()
		override fun registerDataSetObserver(observer: DataSetObserver?)   = dataSetObservable.registerObserver(observer)
		override fun unregisterDataSetObserver(observer: DataSetObserver?) = dataSetObservable.unregisterObserver(observer)
		override fun onGroupCollapsed(groupPosition: Int) {}
		override fun onGroupExpanded(groupPosition: Int)  {}
	}
}




class Frag_Character_Known : android.support.v4.app.Fragment()
{
	private lateinit var viewModel: CharacterKnownViewModel
	private lateinit var adapter: Adapter
	private var showSummary = false
	
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.setHasOptionsMenu(true)
		
		this.viewModel = ViewModelProviders.of(this).get()
		this.adapter   = Adapter()
		
		val character = this.arguments!!.getString(ARG_CHARACTER)
		this.doAsyncButWait {
			this.viewModel.loadData(character)
		}.andThen {
			this.viewModel.data!!.observe(this, this.observerChildren)
		}.runNonExclusive()
	}
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
	{
		return inflater.inflateT<ExpandableListView>(R.layout.frag_page_known, container, false).also {
			it.setGroupIndicator(null)
			it.setAdapter(this.adapter)
			it.setOnChildClickListener{ _, _, gPos, cPos, _ -> this.onListChildClick(gPos, cPos)}
			it.setOnCreateContextMenuListener{menu, _, info -> this.onCreateChildContextMenu(menu, info as ExpandableListView.ExpandableListContextMenuInfo)}
		}
	}
	
	
	override fun onStart()
	{
		super.onStart()
		
		this.showSummary = PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("show_summary_known", false)
		
		val selectionResult = ViewModelProviders.of(this.activity!!).get<SelectionResultViewModel>()
		if(selectionResult.hasKnownSelection)
		{
			val character = this.arguments!!.getString(ARG_CHARACTER)
			val selection = selectionResult.selection!!
			val source    = selectionResult.source
			selectionResult.clear()
			
			this.doAsyncButWait {
				this.onSelectionResult(selection, character, source)
			}.runNonExclusive()
		}
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.options_character_known, menu)
	override fun onOptionsItemSelected(item: MenuItem) : Boolean
	{
		when(item.itemId)
		{
			R.id.menu_action_create -> this.onAddSpellClick()
			
			R.id.menu_action_toggle_summary -> { this.showSummary = !this.showSummary ; this.view!!.cast<ExpandableListView>().invalidateViews() }
			R.id.menu_action_preferences    -> NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_preferences)
		}
		return true
	}
	
	
	private fun onCreateChildContextMenu(menu: ContextMenu, menuInfo: ExpandableListView.ExpandableListContextMenuInfo)
	{
		if(ExpandableListView.getPackedPositionType(menuInfo.packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_CHILD)
			return
		this.inflateMenu(R.menu.context_known, menu)
	}
	
	
	// NOTE: For some reason this function is called even when another fragment is visible
	override fun onContextItemSelected(item: MenuItem): Boolean
	{
		if(!this.userVisibleHint)
			return super.onContextItemSelected(item)
		
		val info = item.menuInfo as ExpandableListView.ExpandableListContextMenuInfo
		if(ExpandableListView.getPackedPositionType(info.packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_CHILD)
			return false
		
		val groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition)
		val childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition)
		val spell = this.adapter.getChild(groupPosition, childPosition).toEntity()
		
		when(item.itemId)
		{
			R.id.menu_action_delete -> { makeConfirmDialog(this.context!!) {
					this.doAsyncButWait{Database.instance?.daoCharacterKnown()?.delete(spell)}.runNonExclusive()
				}.show()
			}
			
			R.id.menu_action_update_study -> { makeIntPickerDialog(this.context!!, spell.time_study, 8){ newValue ->
					spell.time_study = newValue
					this.doAsyncButWait{Database.instance?.daoCharacterKnown()?.update(spell)}.runNonExclusive()
				}.show()
			}
			
			R.id.menu_action_update_copy  -> { makeIntPickerDialog(this.context!!, spell.time_copy, 24){ newValue ->
					spell.time_copy = newValue
					this.doAsyncButWait{Database.instance?.daoCharacterKnown()?.update(spell)}.runNonExclusive()
				}.show()
			}
		}
		return true
	}
	
	
	private fun onListChildClick(groupPosition: Int, childPosition: Int) : Boolean
	{
		val args = Bundle().also{
			it.putString(ARG_SPELL, this.adapter.getChild(groupPosition, childPosition).spell)
		}
		NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_spell, args)
		return true
	}
	
	
	private fun onAddSpellClick()
	{
		ViewModelProviders.of(this.activity!!).get<SelectionResultViewModel>().also { viewModel ->
			viewModel.clear()
			viewModel.target = SelectionTarget.KNOWN
		}
		val args = Bundle().also{
			it.putString(ARG_CHARACTER, this.arguments!!.getString(ARG_CHARACTER))
			it.putInt(ARG_MULTIPLICITY, SelectionType.MULTIPLE.value)
		}
		NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_sources_known, args)
	}
	
	
	private fun onSelectionResult(selection: Collection<SpellSelection>, character: String, source: String)
	{
		assert(selection.isNotEmpty())
		
		val toInsert = selection.map{ EntityKnown(character, source, it.spell) }.toTypedArray()
		Database.instance!!.daoCharacterKnown().insert(*toInsert)
	}
	
	
	
	
	private val observerChildren = android.arch.lifecycle.Observer<List<Known>> { newChildren ->
		this.adapter.children = newChildren?.groupByKnown() ?: HashMap()
		this.adapter.groups = this.adapter.children.mapKnownGroup()
		this.adapter.dataSetObservable.notifyChanged()
	}
	
	// Frag_Character_Known
	private inner class Adapter : android.widget.ExpandableListAdapter
	{
		var groups   : List<KnownGroup> = ArrayList()
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
		
		override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val group = this.getGroup(groupPosition)
			
			val root = convertView ?: context.inflate(R.layout.view_character_known_group, parent, false)
			root.getChildAt<TextView>(0).text = "${group.source} ${group.level}"
			root.getChildAt<TextView>(1).text = group.count.toString()
			return root
		}
		
		@Suppress("DEPRECATION")
		override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View
		{
			val child = this.getChild(groupPosition, childPosition)
			val summaryVisibility = if(this@Frag_Character_Known.showSummary) View.VISIBLE else View.GONE
			val progressText = if(child.time_study < 8) "${child.time_study} / 8" else if(child.time_copy < 24) "${child.time_copy} / 24" else ""
			val backgroundColor = if(child.time_study < 8) getColor(R.color.overlayRed) else if(child.time_copy < 24) getColor(R.color.overlayBlack) else 0

			val root = convertView ?: context.inflate(R.layout.view_character_known_child, parent, false)
			root.getChildAt<TextView>(0).text = child.spell
			root.getChildAt<TextView>(1).apply{ text = Html.fromHtml(child.summary); visibility = summaryVisibility }
			root.getChildAt<TextView>(2).text = progressText
			root.setBackgroundColor(backgroundColor)
			return root
		}
		
		val dataSetObservable = DataSetObservable()
		override fun registerDataSetObserver(observer: DataSetObserver?)   = dataSetObservable.registerObserver(observer)
		override fun unregisterDataSetObserver(observer: DataSetObserver?) = dataSetObservable.unregisterObserver(observer)
		override fun onGroupCollapsed(groupPosition: Int) {}
		override fun onGroupExpanded(groupPosition: Int)  {}
	}
}




private val Scroll.Type.iconRes : Int get() = when(this)
{
	Scroll.Type.None   -> android.R.drawable.star_off
	Scroll.Type.Arcane -> R.drawable.ic_spell_arcane
	Scroll.Type.Divine -> R.drawable.ic_spell_divine
}
