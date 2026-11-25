package com.supavault.app

import android.app.Application
import com.supavault.app.data.supabase.SupabaseManager

class SupaVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseManager.initialize(this)
    }
}
