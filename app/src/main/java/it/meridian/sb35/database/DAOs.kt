package it.meridian.sb35.database

import android.app.SearchManager
import android.arch.persistence.db.SimpleSQLiteQuery
import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.room.*
import android.database.Cursor


typealias LiveData<T>        = android.arch.lifecycle.LiveData<T>
typealias MutableLiveData<T> = android.arch.lifecycle.LiveData<T>


abstract class ADao<T>
{
	@Insert abstract fun insert(vararg entities: T) : LongArray
	@Update abstract fun update(vararg entities: T) : Int
	@Delete abstract fun delete(vararg entities: T) : Int
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	abstract fun replace(vararg entities: T) : LongArray
}


@Dao
abstract class DaoCharacter : ADao<EntityCharacter>()
{
	@Query("SELECT * FROM character")
	abstract fun select() : MutableLiveData<List<Character>>
	
	@Query("SELECT * FROM character")
	abstract fun export() : List<EntityCharacter>
}




@Dao
abstract class DaoCharacterScroll : ADao<EntityScroll>()
{
	@Query("SELECT scroll.*, spell.summary AS summary " +
		       "FROM character_scroll scroll          " +
		       "JOIN spell            spell           " +
		       "ON scroll.spell = spell.name          " +
		       "WHERE scroll.character = :character   " +
		       "ORDER BY scroll.caster_level, scroll.spell, scroll.type")
	abstract fun select(character: String) : MutableLiveData<List<Scroll>>
	
	@Query("SELECT * FROM character_scroll")
	abstract fun export() : List<EntityScroll>
}




@Dao
abstract class DaoCharacterSlot : ADao<EntitySlot>()
{
	@Query("SELECT slot.*, spell.summary AS summary " +
		       "FROM character_slot slot            " +
		       "LEFT JOIN spell     spell           " +
		       "ON slot.spell = spell.name          " +
		       "WHERE slot.character = :character   " +
		       "ORDER BY slot.level, slot.special DESC, slot.spell IS NULL, slot.spell")
	abstract fun select(character: String) : MutableLiveData<List<Slot>>
	
	@Query("SELECT * FROM character_slot")
	abstract fun export() : List<EntitySlot>
}




@Dao
abstract class DaoCharacterKnown : ADao<EntityKnown>()
{
	@Query("SELECT known.*, sList.level AS level, spell.summary AS summary   " +
		       "FROM character_known known                                   " +
		       "JOIN source_spell    sList                                   " +
		       "ON known.source = sList.source AND known.spell = sList.spell " +
		       "JOIN spell                                                   " +
		       "ON known.spell = spell.name                                  " +
		       "WHERE known.character = :character                           " +
		       "ORDER BY known.source, sList.level, known.spell")
	abstract fun select(character: String) : MutableLiveData<List<Known>>
	
	@Query("SELECT known.*, sList.level AS level, spell.summary AS summary   " +
		       "FROM character_known known " +
		       "JOIN source_spell    sList " +
		       "ON known.source = sList.source AND known.spell = sList.spell " +
		       "JOIN spell                                                   " +
		       "ON known.spell = spell.name                                  " +
		       "WHERE known.character = :character " +
		       "AND known.time_study >= 8          " +
		       "AND known.time_copy >= 24          " +
		       "ORDER BY known.source, sList.level, known.spell")
	abstract fun selectPreparable(character: String) : MutableLiveData<List<Known>>
	
	@Query("SELECT * FROM character_known")
	abstract fun export() : List<EntityKnown>
}




@Dao
abstract class DaoSource : ADao<EntitySource>()
{
	@Query("SELECT source.id, source.name, source.type " +
		       "FROM source                            " +
		       "WHERE source.disabled = 0 AND source.type = :type")
	abstract fun select(type: Int) : List<Source>
}




@Dao
abstract class DaoSourceSpell : ADao<EntitySourceSpell>()
{
	@Query("SELECT sList.spell AS name, spell.summary, sList.source, sList.level " +
		       "FROM source_spell sList                                          " +
		       "JOIN spell                                                       " +
		       "ON sList.spell = spell.name                                      " +
		       "WHERE sList.source = :source                                     " +
		       "AND   spell.disabled = 0                                         " +
		       "ORDER BY sList.level, sList.spell")
	abstract fun select(source: String) : List<SourceSpell>
	
	
	@Query("SELECT sList.spell AS name, spell.summary, sList.source, sList.level " +
		       "FROM source_spell sList                                          " +
		       "JOIN spell                                                       " +
		       "ON sList.spell = spell.name                                      " +
		       "WHERE sList.source = :source                                     " +
		       "AND sList.spell NOT IN (SELECT known.spell                                             " +
		       "                        FROM character_known known                                     " +
		       "                        WHERE known.character = :character AND known.source = :source) " +
		       "ORDER BY sList.level, sList.spell")
	abstract fun selectUnknown(source: String, character: String) : List<SourceSpell>
}




@Dao
abstract class DaoSpell : ADao<EntitySpell>()
{
	@Query("SELECT * FROM spell")
	abstract fun select() : MutableLiveData<List<Spell>>
}




@Dao
abstract class DaoSpellInfo : ADao<EntitySpellInfo>()
{
	@Query("SELECT * FROM spell_info info   " +
		       "WHERE info.name = :spellName")
	abstract fun select(spellName: String) : SpellInfo
	
	@Query("SELECT sList.source, sList.level  " +
		       "FROM source_spell sList       " +
		       "LEFT JOIN source  source      " +
		       "ON sList.source = source.name " +
		       "WHERE source.disabled = 0 AND sList.spell = :spellName")
	abstract fun selectLevels(spellName: String) : List<SpellInfoLevel>
}





private const val SEARCH_SUGGESTIONS_QUERY =
	"SELECT id AS _id, " +
	"name AS ${SearchManager.SUGGEST_COLUMN_TEXT_1},     " +
	"name AS ${SearchManager.SUGGEST_COLUMN_INTENT_DATA} " +
	"FROM  _fts_spell   " +
	"WHERE name MATCH ? " +
	"COLLATE nocase"

@Dao
abstract class DaoSearchSpells
{
	@RawQuery
	protected abstract fun select(query: SupportSQLiteQuery) : Cursor

	fun select(pattern: String) : Cursor = this.select(SimpleSQLiteQuery(SEARCH_SUGGESTIONS_QUERY, arrayOf(pattern)))
}
