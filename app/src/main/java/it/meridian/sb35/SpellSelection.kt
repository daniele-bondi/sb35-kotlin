package it.meridian.sb35

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import it.meridian.sb35.database.Scroll
import it.meridian.sb35.database.toScrollType


data class SpellSelection(
	var spell  : String,
	var source : String,
	var level  : Int
)


enum class SelectionType(val value: Int)
{
	NONE(0),
	SINGLE(1),
	MULTIPLE(2);
	
	companion object
	{
		internal val map = SelectionType.values().associateBy{it.value}
	}
}
fun Int.toSelectionType() : SelectionType = SelectionType.map[this]!!


enum class SelectionTarget(val value: Int)
{
	NONE(0),
	SCROLL(1),
	SLOT(2),
	KNOWN(3);
}


class SelectionResultViewModel : ViewModel()
{
	var target     : SelectionTarget             = SelectionTarget.NONE
	var selection  : Collection<SpellSelection>? = null
	var arguments  : Bundle?                     = null
	
	val scrollType : Scroll.Type get() = this.arguments!!.getInt(ARG_SCROLL_TYPE).toScrollType()
	val slotId     : Int         get() = this.arguments!!.getInt(ARG_SLOT_ID)
	val source     : String      get() = this.arguments!!.getString(ARG_SOURCE)
	
	val hasScrollSelection : Boolean get() = this.target == SelectionTarget.SCROLL && this.selection?.isNotEmpty() == true
	val hasSlotSelection   : Boolean get() = this.target == SelectionTarget.SLOT   && this.selection?.isNotEmpty() == true
	val hasKnownSelection  : Boolean get() = this.target == SelectionTarget.KNOWN  && this.selection?.isNotEmpty() == true
	
	fun clear()
	{
		this.target    = SelectionTarget.NONE
		this.selection = null
		this.arguments = null
	}
}
