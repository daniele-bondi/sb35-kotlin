package it.meridian.sb35

import android.app.DownloadManager
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import it.meridian.sb35.database.*
import it.meridian.sb35.utils.*




const val REQUEST_CODE_IMPORT_CHARACTERS = 1


class Frag_Characters : android.support.v4.app.Fragment()
{
	private lateinit var viewModel: CharactersViewModel
	private lateinit var adapter: Adapter
	private var optionsMenu: Menu? = null
	
	
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		this.context!!.registerReceiver(this.onDownloadCompleted, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
		this.setHasOptionsMenu(true)
		
		this.viewModel = ViewModelProviders.of(this).get()
		this.adapter   = Adapter()
		this.doAsyncButWait {
			this.viewModel.loadData()
		}.andThen {
			this.viewModel.data?.observe(this, this.observer)
		}.runNonExclusive()
	}
	
	
	override fun onDestroy()
	{
		super.onDestroy()
		this.context!!.unregisterReceiver(this.onDownloadCompleted)
	}
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View?
	{
		return inflater.inflateT<ListView>(R.layout.frag_nav_characters, container, false).also {
			it.isClickable = true
			it.adapter = this.adapter
			it.setOnItemClickListener { _, _, pos, _ -> onListItemClick(pos) }
			this.registerForContextMenu(it)
		}
	}
	
	
	override fun onStart()
	{
		super.onStart()
		this.myActivity.supportActionBar!!.title = this.getString(R.string.app_name)
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.options_characters, menu).also{ this.optionsMenu = menu }
	override fun onPrepareOptionsMenu(menu: Menu)
	{
		val isDatabaseValid = Database.existsFile
//		val isDatabaseValid = Database.isOpen
		menu.findItem(R.id.menu_action_create)?.run { isVisible = isDatabaseValid }
		menu.findItem(R.id.menu_action_export)?.run { isVisible = isDatabaseValid }
		menu.findItem(R.id.menu_action_import)?.run { isVisible = isDatabaseValid }
		menu.findItem(R.id.menu_action_delete)?.run { isVisible = isDatabaseValid }
		menu.findItem(R.id.menu_action_init)?.run   { isVisible = !isDatabaseValid }
	}
	
	
	override fun onOptionsItemSelected(item: MenuItem) : Boolean
	{
		when(item.itemId)
		{
			R.id.menu_action_create ->
			{
				this.showInputDialog(R.string.create_character, null, this::onCreateConfirmed)
			}
			
			R.id.menu_action_export ->
			{
				makeConfirmDialog(this.context!!) {
					val fileName = "${timestampString()}.yaml"
					this.doAsyncButWaitResult{ Application.exportCharacters(this.context!!, fileName) }.andThen{ error ->
						when(error) {
							0 -> Toast.makeText(this.context, this.getString(R.string.character_export_succeeded, fileName), Toast.LENGTH_LONG).show()
							1 -> Toast.makeText(this.context, R.string.character_export_failed_1, Toast.LENGTH_LONG).show()
							2 -> Toast.makeText(this.context, this.getString(R.string.character_export_failed_2, fileName), Toast.LENGTH_LONG).show()
						}
					}.runNonExclusive()
				}.show()
			}
			
			R.id.menu_action_import ->
			{
				val intent = Intent().apply {
					type = "*/*"
					action = Intent.ACTION_GET_CONTENT
				}
				this.startActivityForResult(Intent.createChooser(intent, this.getString(R.string.select_file)), REQUEST_CODE_IMPORT_CHARACTERS)
			}
			
			R.id.menu_action_init ->
			{
				Application.downloadDatabase(this.context!!).also { error ->
					when(error) {
						0 -> Toast.makeText(this.context!!, R.string.database_download_started, Toast.LENGTH_LONG).show()
						1 -> Toast.makeText(this.context!!, R.string.database_download_error_1, Toast.LENGTH_LONG).show()
						2 -> Toast.makeText(this.context!!, R.string.database_download_error_2, Toast.LENGTH_LONG).show()
						3 -> Toast.makeText(this.context!!, R.string.database_download_error_3, Toast.LENGTH_LONG).show()
					}
				}
			}
			
			R.id.menu_action_delete ->
			{
				makeConfirmDialog(this.context!!, R.string.warning_nuke) {
					Application.deleteDatabase()
					this.reloadData()
				}.show()
			}
			
			R.id.menu_action_preferences -> NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_preferences)
		}
		return true
	}
	
	
	override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo)
	{
		this.inflateMenu(R.menu.context_characters, menu)
	}
	
	
	override fun onContextItemSelected(item: MenuItem): Boolean
	{
		val character = this.adapter.getItem(item.menuInfo.cast<AdapterView.AdapterContextMenuInfo>().position)
		when(item.itemId)
		{
			R.id.menu_action_rename -> {
				this.showInputDialog(R.string.rename_character, character.name){ newName -> this.onRenameConfirmed(character, newName) }
			}
			R.id.menu_action_delete -> {
				makeConfirmDialog(this.context!!){ this.onDeleteConfirmed(character) }.show()
			}
		}
		return true
	}
	
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?)
	{
		super.onActivityResult(requestCode, resultCode, intent)
		if(requestCode == REQUEST_CODE_IMPORT_CHARACTERS && resultCode == android.app.Activity.RESULT_OK)
		{
			val fileName = intent!!.data.lastPathSegment.substringAfterLast('/')
			if(!fileName.endsWith(".yaml", true))
			{
				Toast.makeText(this.context, R.string.character_import_not_yaml, Toast.LENGTH_LONG).show()
				return
			}
			
			makeConfirmDialog(this.context!!, R.string.warning_import) {
				this.doAsyncButWaitResult {
					Application.importCharacters(this.context!!, fileName)
				}.andThen { error ->
					when(error) {
						0 -> Toast.makeText(this.context, R.string.character_import_succeeded, Toast.LENGTH_LONG).show()
						1 -> Toast.makeText(this.context, R.string.character_import_failed_1, Toast.LENGTH_LONG).show()
						2 -> Toast.makeText(this.context, this.getString(R.string.character_import_failed_2, fileName), Toast.LENGTH_LONG).show()
					}
				}.runNonExclusive()
			}.show()
		}
	}
	
	
	private fun onCreateConfirmed(newName: String)
	{
		val entity = EntityCharacter(this.viewModel.findFreeId(), newName)
		this.doAsyncButWait{ Database.instance?.daoCharacter()?.insert(entity) }.runNonExclusive()
	}
	
	
	private fun onUpdateMagicPointsMax(character: Character, newValue: Int)
	{
		val entity = character.toEntity().apply{ spell_points_max = newValue }
		this.doAsyncButWait{ Database.instance?.daoCharacter()?.update(entity) }.runNonExclusive()
	}
	
	
	private fun onRenameConfirmed(character: Character, newName: String)
	{
		if(character.name == newName)
			return
		val entity = character.toEntity().apply{ name = newName }
		this.doAsyncButWait{ Database.instance?.daoCharacter()?.update(entity) }.runNonExclusive()
	}
	
	
	private fun onDeleteConfirmed(character: Character)
	{
		val entity = character.toEntity()
		this.doAsyncButWait{ Database.instance?.daoCharacter()?.delete(entity) }.runNonExclusive()
	}
	
	
	private fun onListItemClick(pos: Int)
	{
		val args = Bundle().also{ it.putString(ARG_CHARACTER, this.adapter.getItem(pos).name) }
		NavHostFragment.findNavController(this).navigate(R.id.nav_action_to_character, args)
	}
	
	
	private fun reloadData()
	{
		this.viewModel.data?.removeObserver(this.observer)
		this.doAsyncButWait {
			this.viewModel.loadData()
		}.andThen {
			if(this.viewModel.data != null)
				this.viewModel.data!!.observe(this, this.observer)
			else
				this.observer.onChanged(null)
		}.runNonExclusive()
	}
	
	
	// NOTE: https://readyset.build/kotlins-sam-problem-f315ffe6be3a
	private val onDownloadCompleted = object: BroadcastReceiver()
	{
		// NOTE: This is supposed to be called even if the user cancels the download
		override fun onReceive(context: Context, intent: Intent)
		{
			val downloadId: Long = Application.databaseDownloadId!!
			Application.databaseDownloadId = null
			
			val manager = Application.instance.getSystemService(Context.DOWNLOAD_SERVICE).cast<DownloadManager>()
			val query  = DownloadManager.Query().apply{ setFilterById(downloadId) }
			val cursor = manager.query(query).apply{ moveToFirst() }
			val status = cursor.getInt(DownloadManager.COLUMN_STATUS)
			cursor.close()
			if(status != DownloadManager.STATUS_SUCCESSFUL)
				return
			
			this@Frag_Characters.viewModel.data?.removeObserver(this@Frag_Characters.observer)
			this@Frag_Characters.doAsyncButWait {
				Application.importDatabase(Application.defaultDatabaseDownloadFile)
				Application.defaultDatabaseDownloadFile.delete()
				this@Frag_Characters.viewModel.loadData()
			}.andThen {
				assert(Database.existsFile)
				this@Frag_Characters.viewModel.data?.observe(this@Frag_Characters, this@Frag_Characters.observer)
				this@Frag_Characters.onPrepareOptionsMenu(this@Frag_Characters.optionsMenu!!)
			}.butIfCanceled {
				this@Frag_Characters.onPrepareOptionsMenu(this@Frag_Characters.optionsMenu!!)
			}.runNonExclusive()
		}
	}
	
	
	
	
	private val observer = android.arch.lifecycle.Observer<List<Character>> { newData ->
		this.adapter.data = newData ?: ArrayList()
		this.adapter.dataSetObservable.notifyChanged()
	}
	
	private inner class Adapter : android.widget.ListAdapter
	{
		var data: List<Character> = ArrayList()
		
		override fun isEnabled(position: Int)       : Boolean   = true
		override fun areAllItemsEnabled()           : Boolean   = true
		override fun hasStableIds()                 : Boolean   = true
		override fun getItemViewType(position: Int) : Int       = 0
		override fun getViewTypeCount()             : Int       = 1
		override fun isEmpty()                      : Boolean   = this.data.isEmpty()
		override fun getCount()                     : Int       = this.data.size
		override fun getItemId(position: Int)       : Long      = this.data[position].id.toLong()
		override fun getItem(position: Int)         : Character = this.data[position]
		
		
		override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
		{
			val item = this.data[position]
			
			val view = (convertView ?: context.inflate(R.layout.view_characters_item, parent, false)) as TextView
			view.text = item.name
			return view
		}
		
		
		val dataSetObservable = DataSetObservable()
		override fun registerDataSetObserver(observer: DataSetObserver?)   = dataSetObservable.registerObserver(observer)
		override fun unregisterDataSetObserver(observer: DataSetObserver?) = dataSetObservable.unregisterObserver(observer)
	}
}
