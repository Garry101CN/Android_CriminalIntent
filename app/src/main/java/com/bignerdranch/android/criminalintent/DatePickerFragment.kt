package com.bignerdranch.android.criminalintent


import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*
private const val ARG_DATE = "date"

/**
 * DatePicker对话框Fragment
 * @author Xilai Jiang
 * @version 1.1
 */
class DatePickerFragment : DialogFragment() {

    /**
     * 回调，交给CrimeFragment实现
     */
    interface Callbacks{
        fun onDateSelected(date: Date)
    }

    /**
     * Dialog生命周期函数
     * @param savedInstanceState Bundle? 缓存
     * @return Dialog 创建好的对话框
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dateListener = DatePickerDialog.OnDateSetListener{ _: DatePicker, year: Int, month: Int, day: Int ->
            val resultDate: Date = GregorianCalendar(year, month, day).time

            targetFragment?.let{ fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }

        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog( // 这里一定要创建DatePickerDialog
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }

    /**
     * 伴生类，创建新的DatePickerFragment
     * 传入当前date，用于初始化picker
     */
    companion object{
        fun newInstance(date: Date): DatePickerFragment{
            val args = Bundle().apply{
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }

}