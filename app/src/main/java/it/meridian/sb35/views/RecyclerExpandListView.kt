package it.meridian.sb35.views

import android.view.ViewGroup


// https://www.bignerdranch.com/blog/expand-a-recyclerview-in-four-steps/
typealias AdapterExpandGroupViewFactory<V> = (ViewGroup, Int) -> V
typealias AdapterExpandChildViewFactory<V> = (ViewGroup, Int) -> V
typealias AdapterExpandGroupViewBinder<D, V> = (D, V) -> Unit
typealias AdapterExpandChildViewBinder<D, V> = (D, V) -> Unit
