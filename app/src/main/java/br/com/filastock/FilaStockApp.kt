package br.com.filastock

import android.app.Application
import br.com.filastock.data.AppDatabase
import br.com.filastock.data.SpoolRepository

class FilaStockApp : Application() {
    val repository: SpoolRepository by lazy { SpoolRepository(AppDatabase.get(this)) }

    override fun onCreate() {
        super.onCreate()
        Notifier.createChannel(this)
    }
}
