package com.servicetick.android.library.fragment

import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.servicetick.android.library.R

open class BaseFragment : Fragment() {

    protected fun showSnackBar(@StringRes messageRes: Int, view: View? = null, dismiss: Boolean = false, duration: Int = Snackbar.LENGTH_LONG): Snackbar? {
        return showSnackBar(getString(messageRes), view, dismiss, duration)
    }

    protected fun showSnackBar(message: String?, view: View? = null, dismiss: Boolean = false, duration: Int = Snackbar.LENGTH_LONG): Snackbar? {

        val snackBar = Snackbar.make(view ?: requireActivity().findViewById(android.R.id.content), message?:"", duration)
        return snackBar.run {
            if (dismiss) {
                setAction(getString(R.string.dismiss)) { dismiss() }
            }
            show()
            this
        }
    }
}
