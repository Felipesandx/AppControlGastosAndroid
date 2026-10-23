package edu.umg.appcontrolgastos

import android.app.Application
import androidx.room.Room
import edu.umg.appcontrolgastos.data.AppDatabase
import edu.umg.appcontrolgastos.data.TransactionRepositoryRoom
import umg.edu.gt.domain.service.FinanceService

class ControlGastosApp : Application() {

    // Instancia global de tu servicio de finanzas
    lateinit var financeService: FinanceService
        private set

    override fun onCreate() {
        super.onCreate()

        // 1. Construir la base de datos SQLite con Room
        val database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "control_gastos_db"
        ).build()

        // 2. Conectar Room con tu Repositorio
        val repository = TransactionRepositoryRoom(database.transactionDao())

        // 3. Inicializar tu servicio de dominio puro
        financeService = FinanceService(repository)
    }
}