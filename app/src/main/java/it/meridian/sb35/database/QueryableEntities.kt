@file:Suppress("PropertyName")

package it.meridian.sb35.database

import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import it.meridian.sb35.utils.hashCode31

class Character(
	var id               : Int    = 0,
	var name             : String = "",
	var spell_points     : Int    = 0,
	var spell_points_max : Int    = 0
)
fun Character.toEntity() = EntityCharacter(this.id, this.name, this.spell_points, this.spell_points_max)




@TypeConverters(Scroll_Type_Converter::class)
class Scroll(
	var character    : String      = "",
	var id           : Int         = 0,
	var type         : Scroll.Type = Scroll.Type.None,
	var caster_level : Int         = 0,
	var spell        : String      = "",
	var summary      : String      = ""
)
{
	enum class Type(val value: Int)
	{
		None(0), Arcane(1), Divine(2);
		companion object { internal val map = Type.values().associateBy{it.value} }
	}
}
fun Scroll.toEntity()            : EntityScroll           = EntityScroll(this.character, this.id, this.type.value, this.caster_level, this.spell)
fun Int.toScrollType()           : Scroll.Type            = Scroll.Type.map[this]!!
fun List<Scroll>.groupByScroll() : Map<Int, List<Scroll>> = this.groupBy{ it.caster_level }

@TypeConverters(Scroll_Type_Converter::class)
class ScrollGroup(
	var caster_level : Int = 0,
	var count        : Int = 0
)
fun Map<Int, List<Scroll>>.mapScrollGroup() : List<ScrollGroup> = this.map{ ScrollGroup(it.key, it.value.size) }




class Slot(
	var character : String  = "",
	var id        : Int     = 0,
	var level     : Int     = 0,
	var spell     : String? = null,
	var summary   : String? = null,
	var expended  : Boolean = false,
	var special   : Boolean = false
)
fun Slot.toEntity()          : EntitySlot           = EntitySlot(this.character, this.id, this.level, this.spell, this.expended, this.special)
fun List<Slot>.groupBySlot() : Map<Int, List<Slot>> = this.groupBy{ it.level }

class SlotGroup(
	var level : Int = 0,
	var count : Int = 0,
	var avail : Int = 0
)
fun Map<Int, List<Slot>>.mapSlotGroup() : List<SlotGroup> = this.map{ SlotGroup(it.key, it.value.size, it.value.count{!it.expended}) }




class Known(
	var character  : String = "",
	var source     : String = "",
	var spell      : String = "",
	var summary    : String = "",
	var level      : Int    = 0,
	var time_study : Int    = 0,
	var time_copy  : Int    = 0
)
val Known.id      : Int get() = "${this.source} ${this.spell}".hashCode31()
val Known.groupId : Int get() = "${this.source} ${this.level}".hashCode31()
fun Known.toEntity()           : EntityKnown           = EntityKnown(this.character, this.source, this.spell, this.time_study, this.time_copy)
fun List<Known>.groupByKnown() : Map<Int, List<Known>> = this.groupBy{ it.groupId }

class KnownGroup(
	var source : String = "",
	var level  : Int    = 0,
	var count  : Int    = 0
)
val KnownGroup.id: Int get() = "${this.source} ${this.level}".hashCode31()
fun Map<Int, List<Known>>.mapKnownGroup() : List<KnownGroup> = this.map{ KnownGroup(it.value.first().source, it.value.first().level, it.value.size) }




class Source(
	var id       : Int    = 0,
	var name     : String = "",
	var type     : Int    = 0
)




class SourceSpell(
	var name    : String = "",
	var summary : String = "",
	var source  : String = "",
	var level   : Int    = 0
)
val SourceSpell.id: Long get() = this.name.hashCode31().toLong()
fun List<SourceSpell>.groupBySourceSpell() : Map<Int, List<SourceSpell>> = this.groupBy{ it.level }

class SourceSpellGroup(
	var source : String = "",
	var level  : Int    = 0,
	var count  : Int    = 0
)
fun Map<Int, List<SourceSpell>>.mapSourceSpellGroup() : List<SourceSpellGroup> = this.map{ SourceSpellGroup(it.value.first().source, it.key, it.value.size) }




class Spell
{
	var id       : Int     = 0
	var name     : String  = ""
	var summary  : String? = null
	var disabled : Boolean = false
}




class SpellInfo
{
	var name         : String  = ""
	var book         : String  = ""
	var page         : Int?    = null
	var school       : String  = ""
	var subschool    : String? = null
	var descriptors  : String? = null
	var components   : String  = ""
	var cast_time    : String  = ""
	var range        : String? = null
	var effect_type  : String? = null
	var effect       : String? = null
	var duration     : String  = ""
	var saving_throw : String? = null
	var resistance   : String? = null
	var fluff        : String? = null
	var description  : String  = ""
}

class SpellInfoLevel
{
	var source : String = ""
	var level  : Int    = 0
}




//class SpellSuggestion
//{
//	var _id                 : Int = 0
//	var suggest_text_1      : String = ""
//	var suggest_intent_data : String = ""
//}






class Scroll_Type_Converter
{
	@TypeConverter fun fromInt(value: Int) : Scroll.Type = value.toScrollType()
}
