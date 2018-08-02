package it.meridian.sb35.utils

import android.os.Parcel
import android.os.Parcelable
import it.meridian.sb35.SpellSelection


//abstract class SpellSelectionSet(protected val content: Set<SpellSelection>) : Set<SpellSelection> by content, Parcelable
//{
//	override fun describeContents() : Int = 0
//	override fun writeToParcel(parcel: Parcel, flags: Int)
//	{
////		val sortedContent: List<SourceSpell> = this.content.toList()
//		parcel.writeInt(this.content.size)
//		parcel.writeStringArray(this.content.map{it.spell}.toTypedArray())
//		parcel.writeStringArray(this.content.map{it.source}.toTypedArray())
//		parcel.writeIntArray(this.content.map{it.level}.toIntArray())
//	}
//
//
//	companion object CREATOR: Parcelable.Creator<SpellSelectionResult>
//	{
//		override fun createFromParcel(parcel: Parcel) : SpellSelectionResult
//		{
//			val size = parcel.readInt()
//			val spells  = Array(size){ "" }
//			val sources = Array(size){ "" }
//			val levels  = IntArray(size)
//
//			parcel.readStringArray(spells)
//			parcel.readStringArray(sources)
//			parcel.readIntArray(levels)
//
//			val content = HashSet<SpellSelection>(size)
//			for(i in 0 until size) {
//				SpellSelection(spells[i], sources[i], levels[i]).also {
//					content.add(it)
//				}
//			}
//			return SpellSelectionResult(content)
//		}
//
//		override fun newArray(size: Int) : Array<SpellSelectionResult?>
//		{
//			return Array(size){null}
//		}
//	}
//}


class SpellSelectionResult(private val content: MutableSet<SpellSelection> = HashSet()) : MutableSet<SpellSelection> by content, Parcelable
{
//	override fun add(element: SpellSelection)                    : Boolean                         = this.content.add(element)
//	override fun addAll(elements: Collection<SpellSelection>)    : Boolean                         = this.content.addAll(elements)
//	override fun clear()                                                                           = this.content.clear()
//	override fun iterator()                                      : MutableIterator<SpellSelection> = this.content.iterator()
//	override fun remove(element: SpellSelection)                 : Boolean                         = this.content.remove(element)
//	override fun removeAll(elements: Collection<SpellSelection>) : Boolean                         = this.content.removeAll(elements)
//	override fun retainAll(elements: Collection<SpellSelection>) : Boolean                         = this.content.retainAll(elements)
	
	
	override fun describeContents() : Int = 0
	override fun writeToParcel(parcel: Parcel, flags: Int)
	{
//		val sortedContent: List<SourceSpell> = this.content.toList()
		parcel.writeInt(this.content.size)
		parcel.writeStringArray(this.content.map{it.spell}.toTypedArray())
		parcel.writeStringArray(this.content.map{it.source}.toTypedArray())
		parcel.writeIntArray(this.content.map{it.level}.toIntArray())
	}
	
	
	companion object CREATOR: Parcelable.Creator<SpellSelectionResult>
	{
		override fun createFromParcel(parcel: Parcel) : SpellSelectionResult
		{
			val size = parcel.readInt()
			val spells  = Array(size){ "" }
			val sources = Array(size){ "" }
			val levels  = IntArray(size)
			
			parcel.readStringArray(spells)
			parcel.readStringArray(sources)
			parcel.readIntArray(levels)
			
			val content = HashSet<SpellSelection>(size)
			for(i in 0 until size) {
				SpellSelection(spells[i], sources[i], levels[i]).also {
					content.add(it)
				}
			}
			return SpellSelectionResult(content)
		}
		
		override fun newArray(size: Int) : Array<SpellSelectionResult?>
		{
			return Array(size){null}
		}
	}
	
}
