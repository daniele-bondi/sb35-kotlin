package it.meridian.sb35.database

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.*

@Entity(tableName = "character",
        primaryKeys = ["id"],
        indices = [Index(value = ["name"], name = "character.name", unique = true)]
//        indices = []
)
data class EntityCharacter(
	var id               : Int,
	var name             : String,
	var spell_points     : Int = 0,
	var spell_points_max : Int = 0
)




@Entity(tableName = "character_scroll",
        primaryKeys = ["character", "id"],
        foreignKeys = [ForeignKey(entity = EntityCharacter::class,
                                  parentColumns = ["name"],
                                  childColumns = ["character"],
                                  onDelete = CASCADE, onUpdate = CASCADE),
                       ForeignKey(entity = EntitySpell::class,
                                  parentColumns = ["name"],
                                  childColumns = ["spell"],
                                  onDelete = RESTRICT, onUpdate = CASCADE)],
        indices = [Index(value = ["spell"], name = "scroll.spell")]
//        indices = []
)
data class EntityScroll(
	var character    : String,
	var id           : Int,
	var type         : Int,
	var caster_level : Int,
	var spell        : String
)




@Entity(tableName = "character_slot",
        primaryKeys = ["character", "id"],
        foreignKeys = [ForeignKey(entity = EntityCharacter::class,
                                  parentColumns = ["name"],
                                  childColumns = ["character"],
                                  onDelete = CASCADE, onUpdate = CASCADE),
	                   ForeignKey(entity = EntitySpell::class,
	                              parentColumns = ["name"],
	                              childColumns = ["spell"],
	                              onDelete = RESTRICT, onUpdate = CASCADE)],
        indices = [Index(value = ["spell"], name = "slot.spell")]
//        indices = [Index(value = ["spell"], name = "slot.spell", unique = true)]
//        indices = []
)
data class EntitySlot(
	var character : String,
	var id        : Int,
	var level     : Int,
	var spell     : String?,
	var expended  : Boolean,
	var special   : Boolean
)




@Entity(tableName = "character_known",
        primaryKeys = ["character", "source", "spell"],
        foreignKeys = [ForeignKey(entity = EntityCharacter::class,
                                  parentColumns = ["name"],
                                  childColumns = ["character"],
                                  onDelete = CASCADE, onUpdate = CASCADE),
                       ForeignKey(entity = EntitySourceSpell::class,
                                  parentColumns = ["source", "spell"],
                                  childColumns = ["source", "spell"],
                                  onDelete = RESTRICT, onUpdate = CASCADE)],
        indices = [Index(value = ["source", "spell"], name = "known.source_spell")]
//        indices = []
)
data class EntityKnown(
	var character  : String,
	var source     : String,
	var spell      : String,
	var time_study : Int = 8,
	var time_copy  : Int = 24
)




@Entity(tableName = "spell",
        primaryKeys = ["id"],
        indices = [Index(value = ["name"], name = "spell.name", unique = true)]
//        indices = []
)
data class EntitySpell(
	var id       : Int,
	var name     : String,
	var summary  : String?,
	var disabled : Boolean
)




@Entity(tableName = "spell_info",
        primaryKeys = ["name"],
        foreignKeys = [ForeignKey(entity = EntitySpell::class,
                                  parentColumns = ["name"],
                                  childColumns = ["name"],
                                  onDelete = RESTRICT, onUpdate = CASCADE),
	                   ForeignKey(entity = EntityBook::class,
	                              parentColumns = ["code"],
	                              childColumns = ["book"],
	                              onUpdate = CASCADE),
	                   ForeignKey(entity = EntitySubschool::class,
				                  parentColumns = ["school", "name"],
				                  childColumns = ["school", "subschool"],
				                  onUpdate = CASCADE)],
        indices = [Index(value = ["book"], name = "spellInfo.book"),
                   Index(value = ["school", "subschool"], name = "spellInfo.school_subschool")]
//        indices = []
)
data class EntitySpellInfo(
	var name          : String,
	var book          : String,
	var page          : Int,
	var school        : String,
	var subschool     : String?,
	var descriptors   : String?,
	var components    : String,
	var cast_time     : String,
	var range         : String?,
	var effect_type   : String?,
	var effect        : String?,
	var duration      : String,
	var saving_throw  : String?,
	var resistance    : String?,
	var fluff         : String?,
	var description   : String
)




@Entity(tableName = "source",
        primaryKeys = ["id"],
        indices = [Index(value = ["name"], name = "source.name", unique = true)]
//        indices = []
)
data class EntitySource(
	var id       : Int,
	var name     : String,
	var type     : Int,
	var disabled : Boolean,
	var prestige : Boolean?
)




@Entity(tableName = "source_spell",
        primaryKeys = ["source", "spell"],
        foreignKeys = [ForeignKey(entity = EntitySource::class,
                                  parentColumns = ["name"],
                                  childColumns = ["source"],
                                  onUpdate = CASCADE),
	                   ForeignKey(entity = EntitySpell::class,
	                              parentColumns = ["name"],
	                              childColumns = ["spell"],
	                              onUpdate = CASCADE)],
        indices = [Index(value = ["spell"], name = "sourceSpell.spell")]
//        indices = []
)
data class EntitySourceSpell(
	var source : String,
	var spell  : String,
	var level  : Int
)




@Entity(tableName = "book",
        primaryKeys = ["code"]
)
data class EntityBook(
	var code : String,
	var name : String
)




@Entity(tableName = "school",
        primaryKeys = ["name"]
)
data class EntitySchool(
	var name : String
)




@Entity(tableName = "school_subschool",
        primaryKeys = ["school", "name"],
        foreignKeys = [ForeignKey(entity = EntitySchool::class,
                                  parentColumns = ["name"],
                                  childColumns = ["school"],
                                  onDelete = RESTRICT, onUpdate = CASCADE)]
)
data class EntitySubschool(
	var school : String,
	var name   : String
)
