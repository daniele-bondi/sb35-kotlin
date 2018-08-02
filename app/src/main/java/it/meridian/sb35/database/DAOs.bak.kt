//package it.meridian.sb35.database
//
//import android.arch.persistence.db.SimpleSQLiteQuery
//import android.arch.persistence.db.SupportSQLiteQuery
//import android.arch.persistence.room.*
//import android.database.Cursor
//import it.meridian.sb35.SEARCH_SUGGESTIONS_QUERY
//
//
//typealias LiveData<T>        = android.arch.lifecycle.LiveData<T>
//typealias MutableLiveData<T> = android.arch.lifecycle.LiveData<T>
//
//
//private interface IDao<T>
//{
//	@Insert fun insert(vararg item: T) : LongArray
//	@Update fun update(vararg item: T) : Int
//	@Delete fun delete(vararg item: T) : Int
//
//}
//
//
//@Dao
//abstract class DaoCharacter(@Suppress("unused") private val db: RoomDatabase) : IDao<EntityCharacter>
//{
//	@Query("SELECT * FROM character")
//	abstract fun select() : MutableLiveData<List<Character>>
//
//	@Query("SELECT * FROM character")
//	abstract fun export() : List<EntityCharacter>
//}
//
//
//
//
//@Dao
//abstract class DaoCharacterScroll(@Suppress("unused") private val db: RoomDatabase) : IDao<EntityScroll>
//{
//	@Query("SELECT scroll.*, spell.summary AS summary " +
//		       "FROM character_scroll scroll          " +
//		       "JOIN spell            spell           " +
//		       "ON scroll.spell = spell.name          " +
//		       "WHERE scroll.character = :character   " +
//		       "ORDER BY scroll.caster_level, scroll.spell, scroll.type")
//	abstract fun select(character: String) : MutableLiveData<List<Scroll>>
//
//	@Query("SELECT scroll.caster_level, COUNT(*) AS count " +
//		       "FROM character_scroll scroll              " +
//		       "WHERE scroll.character = :character       " +
//		       "GROUP BY scroll.caster_level              " +
//		       "ORDER BY scroll.caster_level")
//	abstract fun selectByGroup(character: String) : MutableLiveData<List<ScrollGroup>>
//
//	@Query("SELECT * FROM character_scroll")
//	abstract fun export() : List<EntityScroll>
//}
//
//
//
//
//@Dao
//abstract class DaoCharacterSlot(@Suppress("unused") private val db: RoomDatabase) : IDao<EntitySlot>
//{
//	@Query("SELECT slot.*, spell.summary AS summary " +
//		       "FROM character_slot slot            " +
//		       "LEFT JOIN spell     spell           " +
//		       "ON slot.spell = spell.name          " +
//		       "WHERE slot.character = :character   " +
//		       "ORDER BY slot.level, slot.special DESC, slot.spell IS NULL, slot.spell")
//	abstract fun select(character: String) : MutableLiveData<List<Slot>>
//
//	@Query("SELECT slot.level, COUNT(*) AS count, SUM(slot.expended = 0) as avail " +
//		       "FROM character_slot slot                                          " +
//		       "WHERE slot.character = :character                                 " +
//		       "GROUP BY slot.level                                               " +
//		       "ORDER BY slot.level")
//	abstract fun selectByGroup(character: String) : MutableLiveData<List<SlotGroup>>
//
//	@Query("SELECT * FROM character_slot")
//	abstract fun export() : List<EntitySlot>
//}
//
//
//
//
//@Dao
//abstract class DaoCharacterKnown(@Suppress("unused") private val db: RoomDatabase) : IDao<EntityKnown>
//{
//	@Query("SELECT known.*, sList.level AS level, spell.summary AS summary   " +
//		       "FROM character_known known                                   " +
//		       "JOIN source_spell    sList                                   " +
//		       "ON known.source = sList.source AND known.spell = sList.spell " +
//		       "JOIN spell                                                   " +
//		       "ON known.spell = spell.name                                  " +
//		       "WHERE known.character = :character                           " +
//		       "ORDER BY known.source, sList.level, known.spell")
//	abstract fun select(character: String) : MutableLiveData<List<Known>>
//
//	@Query("SELECT known.source, sList.level, COUNT(*) as count              " +
//		       "FROM character_known known                                   " +
//		       "JOIN source_spell    sList                                   " +
//		       "ON known.source = sList.source AND known.spell = sList.spell " +
//		       "WHERE known.character = :character                           " +
//		       "GROUP BY known.source, sList.level                           " +
//		       "ORDER BY known.source, sList.level")
//	abstract fun selectByGroup(character: String) : MutableLiveData<List<KnownGroup>>
//
//	@Query("SELECT * FROM character_known")
//	abstract fun export() : List<EntityKnown>
//}
//
//
//
//
//@Dao
//abstract class DaoSource(@Suppress("unused") private val db: RoomDatabase) : IDao<EntitySource>
//{
//	@Query("SELECT source.id, source.name, source.type " +
//		       "FROM source                            " +
//		       "WHERE source.disabled = 0 AND source.type = :type")
//	abstract fun select(type: Int) : MutableLiveData<List<Source>>
//}
//
//
//
//
//@Dao
//abstract class DaoSourceSpell(@Suppress("unused") private val db: RoomDatabase) : IDao<EntitySourceSpell>
//{
//	@Query("SELECT list.spell AS name, list.level, spell.summary " +
//		       "FROM source_spell list                           " +
//		       "JOIN spell                                       " +
//		       "ON list.spell = spell.name                       " +
//		       "WHERE list.source = :source                      " +
//		       "AND   spell.disabled = 0                         " +
//		       "ORDER BY list.level, list.spell")
//	abstract fun select(source: String) : MutableLiveData<List<SourceSpell>>
//
//	@Query("SELECT sList.source, sList.level, COUNT(*) as count " +
//		       "FROM source_spell sList                         " +
//		       "WHERE sList.source = :source                    " +
//		       "GROUP BY sList.level                            " +
//		       "ORDER BY sList.level")
//	abstract fun selectByGroup(source: String) : MutableLiveData<List<SourceSpellGroup>>
//
//
//	@Query("SELECT sList.spell AS name, sList.level, spell.summary " +
//		       "FROM source_spell sList                            " +
//		       "JOIN spell                                         " +
//		       "ON sList.spell = spell.name                        " +
//		       "WHERE sList.source = :source                       " +
//		       "AND sList.spell IN (SELECT known.spell                  " +
//		       "                    FROM character_known known          " +
//		       "                    WHERE known.character = :character) " +
//		       "ORDER BY sList.level, sList.spell")
//	abstract fun selectKnown(source: String, character: String) : MutableLiveData<List<SourceSpell>>
//
//	@Query("SELECT sList.source, sList.level, COUNT(*) as count " +
//		       "FROM source_spell sList                         " +
//		       "WHERE sList.source = :source                    " +
//		       "AND sList.spell IN (SELECT known.spell                  " +
//		       "                    FROM character_known known          " +
//		       "                    WHERE known.character = :character) " +
//		       "GROUP BY sList.level                            " +
//		       "ORDER BY sList.level")
//	abstract fun selectKnownByGroup(source: String, character: String) : MutableLiveData<List<SourceSpellGroup>>
//
//
//	@Query("SELECT sList.spell AS name, sList.level, spell.summary " +
//		       "FROM source_spell sList                            " +
//		       "JOIN spell                                         " +
//		       "ON sList.spell = spell.name                        " +
//		       "WHERE sList.source = :source                       " +
//		       "AND sList.spell NOT IN (SELECT known.spell                  " +
//		       "                        FROM character_known known          " +
//		       "                        WHERE known.character = :character) " +
//		       "ORDER BY sList.level, sList.spell")
//	abstract fun selectUnknown(source: String, character: String) : MutableLiveData<List<SourceSpell>>
//
//	@Query("SELECT sList.source, sList.level, COUNT(*) as count " +
//		       "FROM source_spell sList                         " +
//		       "WHERE sList.source = :source                    " +
//		       "AND sList.spell NOT IN (SELECT known.spell                  " +
//		       "                        FROM character_known known          " +
//		       "                        WHERE known.character = :character) " +
//		       "GROUP BY sList.level                            " +
//		       "ORDER BY sList.level")
//	abstract fun selectUnknownByGroup(source: String, character: String) : MutableLiveData<List<SourceSpellGroup>>
//}
//
//
//
//
//@Dao
//abstract class DaoSpell(@Suppress("unused") private val db: RoomDatabase) : IDao<EntitySpell>
//{
//	@Query("SELECT * FROM spell")
//	abstract fun select() : MutableLiveData<List<Spell>>
//}
//
//
//
//
//@Dao
//abstract class DaoSpellInfo(@Suppress("unused") private val db: RoomDatabase) : IDao<EntitySpellInfo>
//{
//	@Query("SELECT * FROM spell_info info   " +
//		       "WHERE info.name = :spellName")
//	abstract fun select(spellName: String) : MutableLiveData<SpellInfo>
//
//	@Query("SELECT sList.source, sList.level  " +
//		       "FROM source_spell sList       " +
//		       "LEFT JOIN source  source      " +
//		       "ON sList.source = source.name " +
//		       "WHERE source.disabled = 0 AND sList.spell = :spellName")
//	abstract fun selectLevels(spellName: String) : MutableLiveData<List<SpellInfoLevel>>
//}
//
//
//
//
//@Dao
//abstract class DaoSearchSpells(@Suppress("unused") private val db: RoomDatabase)
//{
//	@RawQuery
//	protected abstract fun select(query: SupportSQLiteQuery) : Cursor
//
//	fun select(pattern: String) : Cursor = this.select(SimpleSQLiteQuery(SEARCH_SUGGESTIONS_QUERY, arrayOf(pattern)))
//}
//
////@Dao
////abstract class DaoSearchSpells(@Suppress("unused") private val db: RoomDatabase)
////{
////	@Query("SELECT id AS _id, " +
////		       "name AS ${SearchManager.SUGGEST_COLUMN_TEXT_1}, " +
////		       "name AS ${SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID} " +
////		       "FROM  _fts_spell " +
////		       "WHERE name MATCH :pattern " +
////		       "COLLATE nocase")
////	abstract fun select(pattern: String) : Cursor
////}
//
////@Dao
////abstract class DaoSearchSpells(@Suppress("unused") private val db: RoomDatabase)
////{
////	@Query("SELECT id AS _id, " +
////		       "name AS ${SearchManager.SUGGEST_COLUMN_TEXT_1}, " +
////		       "name AS ${SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID} " +
////		       "FROM  spell " +
////		       "WHERE name LIKE :pattern " +
////		       "COLLATE nocase")
////	abstract fun select(pattern: String) : Cursor
////}
