package it.meridian.sb35.utils

import android.os.AsyncTask
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


class AsyncWaitTask<R>(private val waitDialog: WaitDialog?, private val procedure: () -> R)
	: AsyncTask<Unit, Unit, R>()
{
	private var finishCallback : ( (R) -> Unit )? = null
	private var cancelCallback : ( (R) -> Unit )? = null
	
	fun andThen(callback: (R) -> Unit) : AsyncWaitTask<R>
	{
		this.finishCallback = callback
		return this
	}
	
	fun butIfCanceled(callback: (R) -> Unit) : AsyncWaitTask<R>
	{
		this.cancelCallback = callback
		return this
	}
	
	fun runNonExclusive()
	{
		this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
	}
	
	override fun onPreExecute()
	{
		this.waitDialog?.show()
	}
	
	override fun doInBackground(vararg params: Unit?) : R
	{
//		Thread.sleep(3000) // NOTE: for debugging
		return this.procedure()
	}
	
	override fun onPostExecute(result: R)
	{
		this.waitDialog?.dismiss()
		this.finishCallback?.invoke(result)
	}
	
	override fun onCancelled(result: R)
	{
		this.waitDialog?.dismiss()
		this.cancelCallback?.invoke(result)
	}
}


var SHORT_IDS : Map<String, String> = hashMapOf(
	"ACT" to "Australia/Darwin",
	"AET" to "Australia/Sydney",
	"AGT" to "America/Argentina/Buenos_Aires",
	"ART" to "Africa/Cairo",
	"AST" to "America/Anchorage",
	"BET" to "America/Sao_Paulo",
	"BST" to "Asia/Dhaka",
	"CAT" to "Africa/Harare",
	"CNT" to "America/St_Johns",
	"CST" to "America/Chicago",
	"CTT" to "Asia/Shanghai",
	"EAT" to "Africa/Addis_Ababa",
	"ECT" to "Europe/Paris",
	"IET" to "America/Indiana/Indianapolis",
	"IST" to "Asia/Kolkata",
	"JST" to "Asia/Tokyo",
	"MIT" to "Pacific/Apia",
	"NET" to "Asia/Yerevan",
	"NST" to "Pacific/Auckland",
	"PLT" to "Asia/Karachi",
	"PNT" to "America/Phoenix",
	"PRT" to "America/Puerto_Rico",
	"PST" to "America/Los_Angeles",
	"SST" to "Pacific/Guadalcanal",
	"VST" to "Asia/Ho_Chi_Minh",
	"EST" to "-05:00",
	"MST" to "-07:00",
	"HST" to "-10:00"
)


fun timestampString() : String
{
	return SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).apply{timeZone = TimeZone.getDefault()}.format(Date())
}
