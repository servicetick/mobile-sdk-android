package com.servicetick.android.library.fragment

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import javax.inject.Inject

open class BaseFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    open fun <T : ViewModel> getViewModel(modelClass: Class<T>): T = ViewModelProviders.of(this, viewModelFactory).get(modelClass)
}
