package it.meridian.sb35.views

import android.content.Context
import android.content.res.TypedArray
import android.support.v4.math.MathUtils.clamp
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import it.meridian.sb35.R
import kotlin.math.max
import kotlin.math.min



class IntPickerView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : LinearLayout(context, attributeSet, defStyleAttr, defStyleRes)
{
	private var _minValue : Int = 0
	private var _curValue : Int = 0
	private var _maxValue : Int = 0
	
	private val buttonDec : ImageButton
	private val buttonInc : ImageButton
	private val textValue : TextView
	
	val value: Int get() = this._curValue
	
	init
	{
		val attrs: TypedArray? = context.obtainStyledAttributes(attributeSet, R.styleable.IntPickerView, defStyleAttr, defStyleRes)
		if(attrs != null)
		{
			val min = attrs.getInteger(R.styleable.IntPickerView_minValue, 0)
			val max = attrs.getInteger(R.styleable.IntPickerView_maxValue, 0)
			val cur = attrs.getInteger(R.styleable.IntPickerView_curValue, 0)
			
			this._minValue = min(min, max)
			this._maxValue = max(min, max)
			this._curValue = clamp(cur, this._minValue, this._maxValue)
			attrs.recycle()
		}
		
		this.orientation = HORIZONTAL
		this.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
		
		LayoutInflater.from(context).inflate(R.layout.view_int_picker, this, true)
		this.buttonDec = this.getChildAt(0) as ImageButton
		this.textValue = this.getChildAt(1) as TextView
		this.buttonInc = this.getChildAt(2) as ImageButton
		
		this.buttonDec.setOnClickListener{ _ ->  --this._curValue ; this.refresh() }
		this.buttonInc.setOnClickListener{ _ ->  ++this._curValue ; this.refresh() }
		this.refresh()
	}
	
	constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.Spellbook_Widget_IntPicker)
	constructor(context: Context, attributeSet: AttributeSet?)                    : this(context, attributeSet, 0)
	constructor(context: Context)                                                 : this(context, null)
	
	
	fun set(min: Int, cur: Int, max: Int)
	{
		this._minValue = min(min, max)
		this._maxValue = max(min, max)
		this._curValue = clamp(cur, this._minValue, this._maxValue)
		this.refresh()
	}
	
	
	private fun refresh()
	{
		this.buttonDec.isEnabled = (this._minValue < this._curValue)
		this.buttonInc.isEnabled = (this._maxValue > this._curValue)
		this.textValue.text = this._curValue.toString()
	}
}
