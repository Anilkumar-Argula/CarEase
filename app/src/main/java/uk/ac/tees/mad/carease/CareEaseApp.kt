package uk.ac.tees.mad.carease

import android.app.Application

class CareEaseApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
