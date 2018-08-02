package it.meridian.sb35.database

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import java.util.*


class CharactersViewModel(application: Application) : AndroidViewModel(application)
{
	private var _data: MutableLiveData<List<Character>>? = null
	
	fun loadData()
	{
		this._data = Database.instance?.daoCharacter()?.select()
	}
	
	val data : LiveData<List<Character>>? get() = this._data
}

fun CharactersViewModel.findFreeId() : Int
{
	for(hole in 1 .. Int.MAX_VALUE)
	{
		if(this.data!!.value!!.all{it.id != hole})
			return hole
	}
	throw IllegalStateException()
}




class CharacterScrollViewModel(application: Application) : AndroidViewModel(application)
{
	private var _data : MutableLiveData<List<Scroll>>? = null
	
	fun loadData(character: String)
	{
		this._data = Database.instance?.daoCharacterScroll()?.select(character)
	}
	
	val data : LiveData<List<Scroll>>? get() = this._data
}

fun CharacterScrollViewModel.findFreeIds(count: Int) : Queue<Int>
{
	val result = LinkedList<Int>()
	if(count > 0)
	{
		for(hole in 1 .. Int.MAX_VALUE)
		{
			if(this.data!!.value!!.all{it.id != hole})
			{
				result.add(hole)
				if(result.size == count)
					break
			}
		}
		if(result.size != count)
			throw IllegalStateException()
	}
	return result
}




class CharacterSlotViewModel(application: Application) : AndroidViewModel(application)
{
	private var _data : MutableLiveData<List<Slot>>? = null
	
	fun loadData(character: String)
	{
		this._data = Database.instance?.daoCharacterSlot()?.select(character)
	}
	
	val data : LiveData<List<Slot>>? get() = this._data
}

fun CharacterSlotViewModel.findFreeIds(count: Int) : Queue<Int>
{
	val result = LinkedList<Int>()
	if(count > 0)
	{
		for(hole in 1 .. Int.MAX_VALUE)
		{
			if(this.data!!.value!!.all{it.id != hole})
			{
				result.add(hole)
				if(result.size == count)
					break
			}
		}
		if(result.size != count)
			throw IllegalStateException()
	}
	return result
}




class CharacterKnownViewModel(application: Application) : AndroidViewModel(application)
{
	private var _data : MutableLiveData<List<Known>>? = null
	
	fun loadData(character: String)
	{
		this._data = Database.instance?.daoCharacterKnown()?.select(character)
	}
	
	fun loadPreparable(character: String)
	{
		this._data = Database.instance?.daoCharacterKnown()?.selectPreparable(character)
	}
	
	val data : LiveData<List<Known>>? get() = this._data
}




class SourceViewModel(application: Application) : AndroidViewModel(application)
{
	private var _data : List<Source>? = null
	
	fun loadData(type: Int)
	{
		assert(this._data == null)
		this._data = Database.instance?.daoSource()?.select(type)
	}
	
	val data : List<Source>? get() = this._data
}




class SourceSpellViewModel(application: Application) : AndroidViewModel(application)
{
	private var _data : List<SourceSpell>? = null
	
	fun loadData(source: String)
	{
		this._data = Database.instance?.daoSourceSpell()?.select(source)
	}
	
	fun loadDataUnknown(source: String, character: String)
	{
		this._data = Database.instance?.daoSourceSpell()?.selectUnknown(source, character)
	}
	
	val data : List<SourceSpell>? get() = this._data
}




class SpellInfoViewModel(application: Application) : AndroidViewModel(application)
{
	private var _data   : SpellInfo? = null
	private var _levels : List<SpellInfoLevel>? = null
	
	fun loadData(spell: String)
	{
		assert(this._data == null && this._levels == null)
		this._levels = Database.instance?.daoSpellInfo()?.selectLevels(spell)
		this._data   = Database.instance?.daoSpellInfo()?.select(spell)
	}
	
	val data   : SpellInfo?            get() = this._data
	val levels : List<SpellInfoLevel>? get() = this._levels
}
