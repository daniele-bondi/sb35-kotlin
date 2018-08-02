package it.meridian.sb35

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import it.meridian.sb35.database.Database

class SearchSuggestions : ContentProvider()
{
	override fun onCreate() : Boolean = true
	override fun insert(uri: Uri?, values: ContentValues?): Uri? = null
	override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
	override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int = 0
	override fun getType(uri: Uri?): String? = null
	override fun query(uri: Uri?, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?) : Cursor?
	{
		if(selectionArgs!![0].length < 2)
			return null
		// TODO: https://stackoverflow.com/questions/49656009/implementing-search-with-room
		// Add asterisks around the search expression to enable prefix/suffix matching
		selectionArgs.forEachIndexed{ i, arg -> selectionArgs[i] = "*$arg*" }
		val pattern: String = selectionArgs[0]
		val result: Cursor? = Database.instance?.daoSearchSpells()?.select(pattern)
		return result
	}
}
