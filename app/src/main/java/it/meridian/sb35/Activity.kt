package it.meridian.sb35

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import it.meridian.sb35.utils.WaitDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.SearchView
import android.view.MenuItem
import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import it.meridian.sb35.utils.cast




const val ARG_CHARACTER    = "it.meridian.sb35.ARG.character.name"
const val ARG_SPELL        = "it.meridian.sb35.ARG.spell.name"

const val ARG_MULTIPLICITY = "it.meridian.sb35.ARG.selectionMultiplicity"
const val ARG_SCROLL_TYPE  = "it.meridian.sb35.ARG.scroll.type"
const val ARG_SLOT_ID      = "it.meridian.sb35.ARG.slot.id"
const val ARG_SOURCE       = "it.meridian.sb35.ARG.source.name"
const val ARG_SOURCE_TYPE  = "it.meridian.sb35.ARG.source.type"

// https://developer.android.com/topic/libraries/architecture/navigation/navigation-implementing#kotlin
class Activity : AppCompatActivity()
{
	var waitDialog: WaitDialog? = null
		private set(value) { field = value }
	
	private var navDrawer : DrawerLayout? = null
	
	
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
//	    this.setContentView(R.layout.activity)
	    this.setContentView(R.layout.activity_content)
	    this.setSupportActionBar(this.findViewById(R.id.toolbar))
	    
	    this.waitDialog = WaitDialog(this)
	
//	    this.navDrawer = this.findViewById(R.id.navDrawer)
//	    val navView       = this.findViewById<NavigationView>(R.id.navigation_view)
//	    val navController = Navigation.findNavController(this, R.id.navigation_host)
//	    NavigationUI.setupWithNavController(navView, navController)
    }
	
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean
	{
		this.menuInflater.inflate(R.menu.options_activity, menu)
		
		// Associate searchable configuration with the SearchView
		val searchableInfo = this.getSystemService(Context.SEARCH_SERVICE).cast<SearchManager>().getSearchableInfo(this.componentName)
		menu.findItem(R.id.menu_action_search).actionView.cast<SearchView>().apply {
			setSearchableInfo(searchableInfo)
		}
		return true
	}
	
	
	override fun onOptionsItemSelected(item: MenuItem) : Boolean
	{
		when(item.itemId)
		{
			// This will execute only if there is not enough space to
			// show the SearchView inside the Action Bar
			R.id.menu_action_search      -> this.onSearchRequested()
			R.id.menu_action_preferences -> Navigation.findNavController(this, R.id.navigation_host).navigate(R.id.nav_action_to_preferences)
			else -> return super.onOptionsItemSelected(item)
		}
		return true
	}
	
	
	override fun onBackPressed()
	{
		if(this.navDrawer?.isDrawerOpen(GravityCompat.START) == true)
			this.navDrawer?.closeDrawer(GravityCompat.START)
		else
			super.onBackPressed()
	}
	
	
	override fun onSupportNavigateUp(): Boolean
	{
		return Navigation.findNavController(this, R.id.navigation_host).navigateUp()
//		val dest = Navigation.findNavController(this, R.id.navigation_host).currentDestination
//		return Navigation.findNavController(this, R.id.navigation_host).popBackStack()
	}
	
	
	override fun onNewIntent(intent: Intent)
	{
		if(intent.action == "it.meridian.sb35.SUGGESTION_SELECTED")
		{
			val args = Bundle().apply {
				putString(ARG_SPELL, intent.data.toString())
			}
			Navigation.findNavController(this, R.id.navigation_host).navigate(R.id.frag_nav_spell, args)
		}
	}
	
	
	fun onDatabaseOpened()
	{
		val frag = this.supportFragmentManager!!.fragments!![0]!!.childFragmentManager.fragments!![0]!!
		if(frag is IDatabaseConsumer)
			frag.onDatabaseOpened()
	}
}


interface IDatabaseConsumer
{
	fun onDatabaseOpened()
}
