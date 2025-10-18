package com.docai.healthapp

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleUnitTest {
    
    @Test
    fun testPermissionsInManifest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        
        val permissions = packageInfo.requestedPermissions
        assertNotNull("Permissions should not be null", permissions)
        
        // Check for required permissions
        assertTrue("Should have INTERNET permission", 
            permissions?.contains("android.permission.INTERNET") == true)
        assertTrue("Should have ACTIVITY_RECOGNITION permission", 
            permissions?.contains("android.permission.ACTIVITY_RECOGNITION") == true)
        assertTrue("Should have BODY_SENSORS permission", 
            permissions?.contains("android.permission.BODY_SENSORS") == true)
    }
    
    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DoCAI", appName)
    }
}
