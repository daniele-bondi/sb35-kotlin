package it.meridian.sb35.database

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import it.meridian.sb35.Application


@android.arch.persistence.room.Database(entities = [
	EntityCharacter::class,
	EntityScroll::class,
	EntitySlot::class,
	EntityKnown::class,
	EntitySource::class,
	EntitySourceSpell::class,
	EntityBook::class,
	EntitySchool::class,
	EntitySubschool::class,
	EntitySpell::class,
	EntitySpellInfo::class
], version = 1, exportSchema = false)
abstract class Database : RoomDatabase()
{
	abstract fun daoCharacter()            : DaoCharacter
	abstract fun daoCharacterScroll()      : DaoCharacterScroll
	abstract fun daoCharacterSlot()        : DaoCharacterSlot
	abstract fun daoCharacterKnown()       : DaoCharacterKnown
	abstract fun daoSpell()                : DaoSpell
	abstract fun daoSpellInfo()            : DaoSpellInfo
	abstract fun daoSource()               : DaoSource
	abstract fun daoSourceSpell()          : DaoSourceSpell
	abstract fun daoSearchSpells()         : DaoSearchSpells
	
	
	
	
	companion object
	{
		// Replace the String “www.dropbox.com” with “dl.dropboxusercontent.com”
		const val DOWNLOAD_URI = "https://dl.dropboxusercontent.com/s/nv6473lyzgp2sme/spellbook35-kotlin.sqlite"
		const val NAME         = "spellbook.sqlite"
		
		private var _instance: Database? = null
		
		val instance: Database?
		get() {
			if(Database._instance == null)
				Database.init(Application.instance)
			return Database._instance
		}
		
		val isOpen: Boolean get() = Database._instance?.isOpen == true
		
		val existsFile: Boolean get() = Application.databaseFile.exists()
		
		
		fun close()
		{
			if(Database.isOpen)
			{
				Database._instance!!.close()
				Database._instance = null
			}
		}
		
		
		fun init(context: Context)
		{
			synchronized(Database::class)
			{
				if(Database.existsFile)
				{
					Database._instance = Room.databaseBuilder(context, Database::class.java, Database.NAME).apply {
						addCallback(Application.onCreateDatabase)
					}.build()
				}
				else // FIXME: For debugging
				{
//					Database._instance = Room.inMemoryDatabaseBuilder(context, Database::class.java).apply {
//						addCallback(Application.onCreateDatabase)
//					}.build()
				}
			}
		}
	}
}
