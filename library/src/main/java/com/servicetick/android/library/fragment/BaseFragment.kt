package com.servicetick.android.library.fragment

import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.servicetick.android.library.R
import javax.inject.Inject

open class BaseFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    open fun <T : ViewModel> getViewModel(modelClass: Class<T>): T = ViewModelProviders.of(this, viewModelFactory).get(modelClass)

    fun showSnackBar(@StringRes messageRes: Int, view: View? = null, dismiss: Boolean = false, duration: Int = Snackbar.LENGTH_LONG): Snackbar? {
        return showSnackBar(getString(messageRes), view, dismiss, duration)
    }

    fun showSnackBar(message: String?, view: View? = null, dismiss: Boolean = false, duration: Int = Snackbar.LENGTH_LONG): Snackbar? {

        message?.let { msg ->

            val snackBar = Snackbar.make(view ?: requireActivity().findViewById(android.R.id.content), msg, duration)
            snackBar.run {
                if (dismiss) {
                    setAction(getString(R.string.dismiss)) { dismiss() }
                }
                show()
            }
            return snackBar
        }
    }
}
