package it.meridian.sb35.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup



class RecyclerSimpleListView<D, V : View>(context: Context) : RecyclerView(context)
{
	init
	{
		this.layoutManager = LinearLayoutManager(context)
	}
}


class AdapterViewHolder<V : View>(val view: V?) : RecyclerView.ViewHolder(view)


typealias AdapterSimpleViewFactory<V> = (ViewGroup, Int) -> V
typealias AdapterSimpleViewBinder<D, V> = (D, V) -> Unit

class AdapterSimpleList<D, V : View>(
	private var factory: AdapterSimpleViewFactory<V>,
	private var binder: AdapterSimpleViewBinder<D, V>
): RecyclerView.Adapter<AdapterViewHolder<V>>()
{
	private var data : List<D>? = null
	
	
	fun setData(data: List<D>?)
	{
		this.data = data
		this.notifyDataSetChanged()
	}
	
	
	override fun getItemCount(): Int = this.data?.size ?: 0
	
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : AdapterViewHolder<V>
	{
		return AdapterViewHolder(this.factory.invoke(parent, viewType))
	}
	
	
	override fun onBindViewHolder(holder: AdapterViewHolder<V>, position: Int)
	{
		this.binder.invoke(this.data!![position], holder.view!!)
	}
}
