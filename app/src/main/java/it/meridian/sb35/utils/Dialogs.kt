package it.meridian.sb35.utils

import android.content.Context
import android.content.DialogInterface
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import it.meridian.sb35.R
import it.meridian.sb35.views.IntPickerView


// TODO: Maybe look into DialogFragment
class WaitDialog(context: Context) : DialogInterface
{
	private var counter: Int = 0
	private val impl: AlertDialog
	
	init
	{
		val progressRing = ProgressBar(context).also {
			it.isIndeterminate = true
			it.setPadding(0, 0, 0, 0)
		}
		
		this.impl = AlertDialog.Builder(context).also {
			it.setView(progressRing)
		}.create()
		this.impl.setCancelable(false)
	}
	
	@Synchronized
	fun show()
	{
		if(this.counter == 0)
			this.impl.show()
		++this.counter
	}
	
	@Synchronized
	override fun dismiss()
	{
		--this.counter
		if(this.counter == 0)
			this.impl.dismiss()
	}
	
	@Synchronized
	override fun cancel()
	{
		throw NotImplementedError("Cannot cancel WaitDialog")
	}
}




fun makeConfirmDialog(context: Context, @StringRes titleRes: Int = R.string.are_you_sure, callback: () -> Unit) : AlertDialog
{
	return AlertDialog.Builder(context).also {
		it.setTitle(titleRes)
		it.setPositiveButton(R.string.ok)     { _, _ -> callback() }
		it.setNegativeButton(R.string.cancel) { _, _ -> }
	}.create()
}




fun makeInputDialog(context: Context, title: String?, initialText: String?, callback: (String) -> Unit) : AlertDialog
{
	val edit = EditText(context).also {
		it.setText(initialText)
		it.setSelectAllOnFocus(true)
		it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
	}
	return AlertDialog.Builder(context).also {
		it.setTitle(title)
		it.setView(edit)
		it.setPositiveButton(R.string.ok)     { _, _ -> callback(edit.text.toString()) }
		it.setNegativeButton(R.string.cancel) { _, _ -> }
	}.create()
}




fun makeRadioDialog(context: Context, @LayoutRes layoutRes: Int, @StringRes titleRes: Int, callback: (Int) -> Unit) : AlertDialog
{
	return AlertDialog.Builder(context).also {
		it.setView(layoutRes)
		it.setTitle(titleRes)
		it.setPositiveButton(R.string.ok)     { d, _ -> callback(d.cast<AlertDialog>().findViewById<RadioGroup>(R.id.radio_group)!!.checkedRadioButtonId) }
		it.setNegativeButton(R.string.cancel) { _, _ -> }
	}.create()
}




fun makeIntPickerDialog(context: Context, initialValue: Int, maxValue: Int, callback: (Int) -> Unit) : AlertDialog
{
	val picker = IntPickerView(context).also {
		it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
		it.gravity = Gravity.CENTER
//		it.setBackgroundResource(R.drawable.background) // FIXME: This expands the dialog to the image size
		it.set(0, initialValue, maxValue)
	}
	return AlertDialog.Builder(context).also {
		it.setView(picker)
		it.setPositiveButton(R.string.ok)     { _, _ -> if(picker.value != initialValue) callback(picker.value) }
		it.setNegativeButton(R.string.cancel) { _, _ -> }
	}.create()
}




private fun getSlotsPickersValues(dialog: DialogInterface) : Map<Int, Int>
{
	return dialog.cast<AlertDialog>().findViewById<ConstraintLayout>(android.R.id.list)!!.let { layout ->
		(0..9).associate{lv -> lv to layout.getChildAt(lv).cast<IntPickerView>().value}
	}
}

fun makeSlotCreationDialog(context: Context, callback: (Map<Int, Int>) -> Unit) : AlertDialog
{
	return AlertDialog.Builder(context).also {
		it.setView(R.layout.dialog_slots_create)
		it.setPositiveButton(R.string.ok)     { d, _ -> callback(getSlotsPickersValues(d)) }
		it.setNegativeButton(R.string.cancel) { _, _ -> }
	}.create()
}
