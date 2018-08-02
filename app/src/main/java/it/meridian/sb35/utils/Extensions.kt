package it.meridian.sb35.utils

import android.app.Dialog
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.LayoutRes
import android.support.annotation.MenuRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import it.meridian.sb35.Activity
import it.meridian.sb35.R




@Suppress("unused")
val __FUNC__: String get() = Throwable().stackTrace[0].methodName
//val __FUNC__: String get() = object{}.javaClass.enclosingMethod.name


inline fun <reified T> Any.cast() : T = this as T


val Long.lsb31 : Long get() = this and 0b00000000_00000000_01111111_11111111
val Long.lsb32 : Long get() = this and 0b00000000_00000000_11111111_11111111


fun String.hashCode31() : Int = this.hashCode() and 0b01111111_11111111


fun <K, V> Map<K, V>.foreach(consumer: (K, V) -> Unit)
{
	for((k, v) in this)
		consumer(k, v)
}


fun Bundle.getOptionalInt(key: String, nullValue: Int = Int.MIN_VALUE) : Int?
{
	return this.getInt(key, nullValue).takeIf{ it != nullValue }
}


inline fun <reified T: View> View.getChildAt(index: Int) : T
{
	return (this as ViewGroup).getChildAt(index) as T
}


fun <T: View> Context?.inflate(@LayoutRes resource: Int, root: ViewGroup?, attachToRoot: Boolean) : T
{
	return LayoutInflater.from(this!!).inflateT(resource, root, attachToRoot)
}


fun <T: View> LayoutInflater.inflateT(@LayoutRes resource: Int, root: ViewGroup?, attachToRoot: Boolean) : T
{
	@Suppress("UNCHECKED_CAST")
	return this.inflate(resource, root, attachToRoot) as T
}


fun Cursor.getInt(column: String) : Int?
{
	return this.getColumnIndex(column).takeIf{colIndex -> colIndex != -1}?.let{colIndex -> this.getInt(colIndex)}
}


inline fun <reified T: ViewModel> ViewModelProvider.get() : T
{
	val modelClass = T::class.java
	val canonicalName =  modelClass.canonicalName ?: throw IllegalArgumentException("Local and anonymous classes can not be ViewModels")
	return this.get<T>("android.arch.lifecycle.ViewModelProvider.DefaultKey:$canonicalName", modelClass)
}


fun Dialog.showKeyboard()
{
	this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
}


val Fragment.myActivity
	get() = this.activity as Activity

fun Fragment.getColor(@ColorRes res: Int) : Int
{
	return this.context?.resources?.getColor(res) ?: 0
}

fun Fragment.inflateMenu(@MenuRes resource: Int, menu: ContextMenu)
{
	return this.activity!!.menuInflater.inflate(resource, menu)
}

fun Fragment.showInputDialog(@StringRes titleRes: Int, content: String?, callback: (String) -> Unit) : AlertDialog
{
	return makeInputDialog(this.context!!, this.getString(titleRes), content, callback).apply { show(); showKeyboard() }
}

fun Fragment.doAsyncButWait(procedure: () -> Unit) = AsyncWaitTask(this.myActivity.waitDialog!!, procedure)
fun <R> Fragment.doAsyncButWaitResult(procedure: () -> R) = AsyncWaitTask(this.myActivity.waitDialog!!, procedure)
