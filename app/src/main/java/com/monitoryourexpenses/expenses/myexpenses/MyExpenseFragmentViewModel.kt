package com.monitoryourexpenses.expenses.myexpenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.monitoryourexpenses.expenses.database.ExpenseMonitorDao
import com.monitoryourexpenses.expenses.utilites.*
import com.monitoryourexpenses.expenses.utilites.MyApp.Companion.context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.threeten.bp.LocalDate
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


class MyExpenseFragmentViewModel(val database:ExpenseMonitorDao,application: Application) : AndroidViewModel(application) {

    private val application = getApplication<Application>().applicationContext
    private val _navigateToMyExpense = MutableLiveData<Boolean>()
    val navigateToMyExpense : LiveData<Boolean>
    get() = _navigateToMyExpense


    private val _todayExpense = MutableLiveData<String>()
    val todayExpense : LiveData<String>
        get() = _todayExpense

    private val _weekExpense = MutableLiveData<String>()
    val weekExpense : LiveData<String>
        get() = _weekExpense


    private val _monthExpense = MutableLiveData<String>()
    val monthExpense : LiveData<String>
        get() = _monthExpense
    
    

    init {
        checkIfDurationFinished()
        getTodayExpenses()
        getWeekExpenses()
        getMonthExpenses()
    }



    private fun getTodayExpenses(){
        viewModelScope.launch {
                database.retrieveTodayExpense(PrefManager.getCurrency(application).toString()).collect {
                        _todayExpense.value = expenseAmountFormatter(it)
                }
        }
    }

    private fun getWeekExpenses(){
        viewModelScope.launch {
            database.retrieveWeekExpense(PrefManager.getCurrency(application).toString()).collect {
                    _weekExpense.value = expenseAmountFormatter(it)
            }
        }
    }

    private fun getMonthExpenses(){
        viewModelScope.launch {
            database.retrieveMonthExpense(PrefManager.getCurrency(application).toString()).collect {
                    _monthExpense.value = expenseAmountFormatter(it)
            }
        }
    }



    fun onFabClicked(){
       _navigateToMyExpense.value = true
    }


    fun onNavigatedToMyExpense(){
        _navigateToMyExpense.value = false
    }



    fun clearPrefs(){
        PrefManager.clear(application)
    }


    private fun checkIfDurationFinished(){
        //today
        val savedCurrentDate = PrefManager.getCurrentDate(context)

        //week
        val endOfWeek = PrefManager.getEndOfTheWeek(context)


        //month
        val endOfMonth = PrefManager.getEndOfTheMonth(context)

        //compare dates
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val savedDate = sdf.parse(savedCurrentDate)
            val currentDate = sdf.parse(LocalDate.now().toString())
            val endOfTheWeek = sdf.parse(endOfWeek)
            val endOfTheMonth = sdf.parse(endOfMonth)



        if (currentDate > savedDate){
            viewModelScope.launch {
                database.updateTodayExpenses(BigDecimal.ZERO,PrefManager.getCurrency(application).toString())
                PrefManager.saveCurrentDate(context,LocalDate.now().toString())
            }
        }

        if (currentDate > endOfTheWeek){
            viewModelScope.launch {
                database.updateWeekExpenses(BigDecimal.ZERO,PrefManager.getCurrency(application).toString())
                PrefManager.saveStartOfTheWeek(context,LocalDate.now().toString())
                PrefManager.saveEndOfTheWeek(application,LocalDate.now().plusDays(7).toString())
            }
        }

        if(currentDate > endOfTheMonth){
            viewModelScope.launch {
                database.updateMonthExpenses(BigDecimal.ZERO,PrefManager.getCurrency(application).toString())
                PrefManager.saveStartOfTheMonth(application,LocalDate.now().toString())
                PrefManager.saveEndOfTheMonth(application,LocalDate.now().plusMonths(1).toString())
            }
        }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}
