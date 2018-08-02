package it.meridian.sb35

import android.app.Activity
import android.app.DownloadManager
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import it.meridian.sb35.database.*
import it.meridian.sb35.utils.cast
import java.io.File


class Application : android.app.Application(), android.app.Application.ActivityLifecycleCallbacks
{
	companion object
	{
		lateinit var instance: Application
		
		var databaseDownloadId: Long? = null
		
		val databaseFile: File
			get() = Application.instance.getDatabasePath(Database.NAME)
		
		val defaultDatabaseDownloadFile: File
			get() = File(Application.instance.getExternalFilesDir(null), Database.NAME + "-init")
		
		val onCreateDatabase = object: RoomDatabase.Callback()
		{
			override fun onCreate(db: SupportSQLiteDatabase)
			{
			
			}
			
			override fun onOpen(db: SupportSQLiteDatabase)
			{
				super.onOpen(db)
				Application.instance.currentActivity?.onDatabaseOpened()
			}
		}
	}
	
	
	var currentActivity: it.meridian.sb35.Activity? = null
	
	
	override fun onCreate()
	{
		super.onCreate()
		Application.instance = this
		this.registerActivityLifecycleCallbacks(this)
		Database.init(this)
	}
	
	
	override fun onTerminate()
	{
		super.onTerminate()
		Database.close()
	}
	
	
	override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
	override fun onActivityStarted(activity: Activity)
	{
		this.currentActivity = activity as it.meridian.sb35.Activity
	}
	override fun onActivityPaused(activity: Activity)                               {}
	override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
	override fun onActivityResumed(activity: Activity)                              {}
	override fun onActivityStopped(activity: Activity)                              {}
	override fun onActivityDestroyed(activity: Activity)                            {}
}


fun Application.Companion.exportCharacters(context: Context, fileName: String) : Int
{
	if(!Database.isOpen)
		return 1
	
	val exportFile = File(context.getExternalFilesDir("exports"), fileName)
	if(!exportFile.createNewFile())
		return 2
	
	val data = ExportData().apply {
		characters       = Database.instance!!.daoCharacter().export()
		character_scroll = Database.instance!!.daoCharacterScroll().export()
		character_slot   = Database.instance!!.daoCharacterSlot().export()
		character_known  = Database.instance!!.daoCharacterKnown().export()
	}
	
	val mapper = ObjectMapper(YAMLFactory()).enable(SerializationFeature.INDENT_OUTPUT)
	mapper.writeValue(exportFile, data)
	return 0
}


fun Application.Companion.importCharacters(context: Context, fileName: String) : Int
{
	if(!Database.isOpen)
		return 1
	
	val importFile = File(context.getExternalFilesDir("exports"), fileName)
	if(!importFile.exists())
		return 2
	
	val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
	val data: ExportData = mapper.readValue(importFile, ExportData::class.java)
	
	Database.instance!!.daoCharacter().replace(*data.characters!!.toTypedArray())
	Database.instance!!.daoCharacterScroll().replace(*data.character_scroll!!.toTypedArray())
	Database.instance!!.daoCharacterSlot().replace(*data.character_slot!!.toTypedArray())
	Database.instance!!.daoCharacterKnown().replace(*data.character_known!!.toTypedArray())
	return 0
}


fun Application.Companion.downloadDatabase(context: Context) : Int
{
	if(Database.existsFile)
		return 1
	
	val netInfo = context.getSystemService(Context.CONNECTIVITY_SERVICE).cast<ConnectivityManager>().activeNetworkInfo
	if(netInfo?.isConnected != true)
		return 2
	
	synchronized(Database::class)
	{
		if(Application.databaseDownloadId != null)
			return 3
		
		val tempFile = Application.defaultDatabaseDownloadFile
		if(tempFile.exists())
			tempFile.delete()
		
		val manager = Application.instance.getSystemService(Context.DOWNLOAD_SERVICE).cast<DownloadManager>()
		val request = DownloadManager.Request(Uri.parse(Database.DOWNLOAD_URI)).also {
			it.setTitle("Downloading empty database")
			it.allowScanningByMediaScanner()
			it.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
			it.setDestinationInExternalFilesDir(context, null, tempFile.name)
		}
		
		Application.databaseDownloadId = manager.enqueue(request)
	}
	
	return 0
}


fun Application.Companion.importDatabase(source: File)
{
	if(source.exists())
		source.copyTo(Application.databaseFile, false, 0xFFFF)
}


fun Application.Companion.deleteDatabase()
{
	Database.close()
	with(Application.databaseFile) {
		if(exists())
			delete()
	}
}




class ExportData
{
	var characters       : List<EntityCharacter>? = null
	var character_scroll : List<EntityScroll>?    = null
	var character_slot   : List<EntitySlot>?      = null
	var character_known  : List<EntityKnown>?     = null
}
